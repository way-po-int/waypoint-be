package waypoint.mvp.place.application;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.dto.PlaceIdLookupCommand;
import waypoint.mvp.place.application.dto.PlaceIdLookupResponse;
import waypoint.mvp.place.application.dto.PlaceIdLookupResult;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.PlaceDetail;
import waypoint.mvp.place.error.PlaceError;
import waypoint.mvp.place.infrastructure.google.GooglePlacesClient;
import waypoint.mvp.place.infrastructure.persistence.PlaceRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlacesLookupService {

	private static final int CONCURRENCY = 5;
	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

	private final GooglePlacesClient googlePlacesClient;
	private final PlaceRepository placeRepository;

	private final ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);

	/**
	 * 입력 queries는 "중복 제거" 후 처리한다.
	 * 응답 results도 중복 제거된 query 기준으로만 반환한다.
	 */
	public PlaceIdLookupResponse lookupAndSavePlaces(PlaceIdLookupCommand command) {
		Set<String> uniqueQueries = command.queries().stream()
			.filter(Objects::nonNull)
			.map(String::trim)
			.filter(q -> !q.isBlank())
			.collect(Collectors.toCollection(LinkedHashSet::new));

		if (uniqueQueries.isEmpty()) {
			return PlaceIdLookupResponse.of(List.of());
		}

		Map<String, PlaceIdLookupResult> resultsByQuery = lookupTop1PlaceIdsByQuery(uniqueQueries);

		List<PlaceIdLookupResult> idResults = uniqueQueries.stream()
			.map(q -> resultsByQuery.getOrDefault(q,
				PlaceIdLookupResult.failure(q, PlaceError.PLACE_ID_NOT_FOUND.getMessage())))
			.toList();

		Set<String> placeIds = idResults.stream()
			.map(PlaceIdLookupResult::placeId)
			.filter(Objects::nonNull)
			.filter(id -> !id.isBlank())
			.collect(Collectors.toCollection(LinkedHashSet::new));

		if (placeIds.isEmpty()) {
			return PlaceIdLookupResponse.of(idResults);
		}

		Set<String> existing = findExistingPlaceIdsSafely(placeIds);

		List<String> newPlaceIds = placeIds.stream()
			.filter(id -> !existing.contains(id))
			.toList();

		if (!newPlaceIds.isEmpty()) {
			List<Place> newPlaces = fetchDetailsAndMapToPlacesSafely(newPlaceIds);
			saveAllIgnoreDuplicate(newPlaces);
		}

		return PlaceIdLookupResponse.of(idResults);
	}

	private Map<String, PlaceIdLookupResult> lookupTop1PlaceIdsByQuery(Set<String> queries) {
		List<CompletableFuture<PlaceIdLookupResult>> futures = new ArrayList<>();

		for (String query : queries) {
			futures.add(CompletableFuture.supplyAsync(() -> {
				try {
					String placeId = googlePlacesClient.searchTop1PlaceId(query);
					if (placeId == null || placeId.isBlank()) {
						return PlaceIdLookupResult.failure(query, PlaceError.PLACE_ID_NOT_FOUND.getMessage());
					}
					return PlaceIdLookupResult.success(query, placeId);
				} catch (Exception e) {
					log.warn("placeId 조회 중 오류가 발생했습니다. query='{}'", query, e);
					String msg = (e.getMessage() == null || e.getMessage().isBlank())
						? "placeId 조회 중 오류가 발생했습니다."
						: e.getMessage();
					return PlaceIdLookupResult.failure(query, msg);
				}
			}, executor));
		}

		return futures.stream()
			.map(CompletableFuture::join)
			.collect(Collectors.toMap(
				PlaceIdLookupResult::query,
				r -> r,
				(a, b) -> a,
				java.util.LinkedHashMap::new
			));
	}

	private Set<String> findExistingPlaceIdsSafely(Set<String> placeIds) {
		if (CollectionUtils.isEmpty(placeIds)) {
			return Set.of();
		}
		return placeRepository.findExistingPlaceIds(placeIds);
	}

	private List<Place> fetchDetailsAndMapToPlacesSafely(List<String> newPlaceIds) {
		List<CompletableFuture<Place>> futures = new ArrayList<>();

		for (String placeId : newPlaceIds) {
			futures.add(CompletableFuture.supplyAsync(() -> {
				try {
					Map<String, Object> raw = googlePlacesClient.getPlaceDetails(placeId);
					return mapToPlace(placeId, raw);
				} catch (Exception e) {
					log.debug("Place details fetch failed. placeId={}", placeId, e);
					return null;
				}
			}, executor));
		}

		return futures.stream()
			.map(CompletableFuture::join)
			.filter(Objects::nonNull)
			.toList();
	}

	private Place mapToPlace(String placeId, Map<String, Object> raw) {
		try {
			String name = extractName(raw);
			String address = extractAddress(raw);
			Point location = extractLocation(raw);

			if (!StringUtils.hasText(name) || !StringUtils.hasText(address) || location == null) {
				throw new BusinessException(PlaceError.PLACE_LOOKUP_FAILED);
			}

			String primaryType = extractPrimaryType(raw);
			String primaryTypeDisplayName = extractPrimaryTypeDisplayName(raw);
			String googleMapsUri = extractGoogleMapsUri(raw);
			String photoName = extractFirstPhotoName(raw);

			PlaceDetail detail = PlaceDetail.create(
				placeId,
				primaryType,
				primaryTypeDisplayName,
				googleMapsUri,
				photoName
			);
			return Place.create(name, address, location, detail);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new BusinessException(PlaceError.PLACE_LOOKUP_FAILED, e);
		}
	}

	protected void saveAllIgnoreDuplicate(List<Place> places) {
		if (CollectionUtils.isEmpty(places)) {
			return;
		}
		try {
			placeRepository.saveAll(places);
		} catch (DataIntegrityViolationException e) {
			log.debug("Duplicate placeId while saving places. size={}", places.size(), e);
		}
	}

	private String extractName(Map<String, Object> raw) {
		Object dn = raw.get("displayName");
		if (dn instanceof Map<?, ?> m) {
			Object text = m.get("text");
			if (text instanceof String s) {
				return s;
			}
		}
		return null;
	}

	private String extractAddress(Map<String, Object> raw) {
		Object addr = raw.get("formattedAddress");
		return (addr instanceof String s) ? s : null;
	}

	private String extractPrimaryType(Map<String, Object> raw) {
		if (raw == null) {
			return null;
		}
		Object v = raw.get("primaryType");
		return (v instanceof String s && StringUtils.hasText(s)) ? s : null;
	}

	private String extractPrimaryTypeDisplayName(Map<String, Object> raw) {
		if (raw == null) {
			return null;
		}
		Object v = raw.get("primaryTypeDisplayName");
		if (v instanceof Map<?, ?> m) {
			Object text = m.get("text");
			if (text instanceof String s && StringUtils.hasText(s)) {
				return s;
			}
		}
		return null;
	}

	private String extractGoogleMapsUri(Map<String, Object> raw) {
		if (raw == null) {
			return null;
		}
		Object v = raw.get("googleMapsUri");
		return (v instanceof String s && StringUtils.hasText(s)) ? s : null;
	}

	private String extractFirstPhotoName(Map<String, Object> raw) {
		if (raw == null) {
			return null;
		}
		Object v = raw.get("photos");
		if (!(v instanceof List<?> photos) || photos.isEmpty()) {
			return null;
		}

		Object first = photos.get(0);
		if (!(first instanceof Map<?, ?> m)) {
			return null;
		}

		Object name = m.get("name");
		return (name instanceof String s && StringUtils.hasText(s)) ? s : null;
	}

	private Point extractLocation(Map<String, Object> raw) {
		Object loc = raw.get("location");
		if (!(loc instanceof Map<?, ?> m)) {
			return null;
		}

		Double lat = toDouble(m.get("latitude"));
		Double lng = toDouble(m.get("longitude"));
		if (lat == null || lng == null) {
			return null;
		}

		return GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
	}

	private Double toDouble(Object o) {
		if (o instanceof Number n) {
			return n.doubleValue();
		}
		if (o instanceof String s) {
			try {
				return Double.parseDouble(s);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	@PreDestroy
	void shutdown() {
		executor.shutdown();
	}
}
