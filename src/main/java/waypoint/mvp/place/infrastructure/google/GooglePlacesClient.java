package waypoint.mvp.place.infrastructure.google;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import waypoint.mvp.place.application.dto.GooglePlaceDetailsDto;
import waypoint.mvp.place.error.SearchFailureCode;
import waypoint.mvp.place.error.exception.PlaceSearchException;

@Component
public class GooglePlacesClient {

	private static final String TEXT_SEARCH_FIELD_MASK = "places.id";
	private static final String DETAILS_FIELD_MASK = "id,displayName,formattedAddress,location,primaryType,googleMapsUri,photos.name";

	private static final int PHOTO_SIZE_MIN = 1;
	private static final int PHOTO_SIZE_MAX = 4800;

	private static final int DEFAULT_TEXT_SEARCH_PAGE_SIZE = 10;

	private final RestClient restClient;
	private final String textSearchPath;
	private final String detailsPath;
	private final int photoMaxWidthPx;

	public GooglePlacesClient(
		RestClient.Builder builder,
		@Value("${google.places.base-url:https://places.googleapis.com}") String baseUrl,
		@Value("${google.places.api-key}") String apiKey,
		@Value("${google.places.text-search-path}") String textSearchPath,
		@Value("${google.places.details-path}") String detailsPath,
		@Value("${google.places.photo.max-width-px:800}") int photoMaxWidthPx
	) {
		this.restClient = builder
			.baseUrl(baseUrl)
			.defaultHeader("X-Goog-Api-Key", apiKey)
			.build();
		this.textSearchPath = textSearchPath;
		this.detailsPath = detailsPath;
		this.photoMaxWidthPx = photoMaxWidthPx;
	}

	public List<String> searchPlaceIds(String textQuery) {
		return searchPlaceIds(textQuery, DEFAULT_TEXT_SEARCH_PAGE_SIZE);
	}

	public List<String> searchPlaceIds(String textQuery, int pageSize) {
		if (!StringUtils.hasText(textQuery)) {
			return List.of();
		}

		String q = textQuery.trim();
		if (!StringUtils.hasText(q)) {
			return List.of();
		}

		int size = Math.max(1, pageSize);

		var response = restClient.post()
			.uri(textSearchPath)
			.header("X-Goog-FieldMask", TEXT_SEARCH_FIELD_MASK)
			.body(Map.of(
				"textQuery", q,
				"pageSize", size,
				"languageCode", "ko",
				"regionCode", "KR"
			))
			.retrieve()
			.onStatus(HttpStatusCode::isError, (req, res) -> {
				throw new PlaceSearchException(SearchFailureCode.PLACES_API_ERROR);
			})
			.body(GooglePlaceIdResponse.class);

		if (response == null || response.places() == null || response.places().isEmpty()) {
			return List.of();
		}

		return response.places().stream()
			.map(GooglePlaceIdResponse.PlaceItem::id)
			.filter(StringUtils::hasText)
			.distinct()
			.toList();
	}

	public Optional<String> searchTop1PlaceId(String textQuery) {
		return searchPlaceIds(textQuery, 1).stream().findFirst();
	}

	public Optional<GooglePlaceDetailsDto> getPlaceDetails(String placeId) {
		if (!StringUtils.hasText(placeId)) {
			return Optional.empty();
		}

		var response = restClient.get()
			.uri(uriBuilder -> uriBuilder
				.path(detailsPath)
				.queryParam("languageCode", "ko")
				.queryParam("regionCode", "KR")
				.build(placeId))
			.header("X-Goog-FieldMask", DETAILS_FIELD_MASK)
			.retrieve()
			.onStatus(HttpStatusCode::isError, (req, res) -> {
				throw new PlaceSearchException(SearchFailureCode.PLACES_API_ERROR);
			})
			.body(GooglePlaceDetailsDto.class);

		return Optional.ofNullable(response);
	}

	public Optional<String> getPhotoUri(String photoName) {
		return getPhotoUri(photoName, photoMaxWidthPx, null);
	}

	public Optional<String> getPhotoUri(String photoName, Integer maxWidthPx, Integer maxHeightPx) {
		String normalized = normalizePhotoName(photoName);
		if (!StringUtils.hasText(normalized)) {
			return Optional.empty();
		}

		Integer width = (maxWidthPx != null) ? validatePhotoSize(maxWidthPx) : null;
		Integer height = (maxHeightPx != null) ? validatePhotoSize(maxHeightPx) : null;

		if (width == null && height == null) {
			width = validatePhotoSize(photoMaxWidthPx);
		}

		String uri = buildPhotoMediaUri(normalized, width, height);

		var response = restClient.get()
			.uri(uri)
			.retrieve()
			.onStatus(HttpStatusCode::isError, (req, res) -> {
				throw new PlaceSearchException(SearchFailureCode.PLACES_API_ERROR);
			})
			.body(PhotoMediaResponse.class);

		if (response == null || !StringUtils.hasText(response.photoUri())) {
			return Optional.empty();
		}
		return Optional.of(response.photoUri());
	}

	private String buildPhotoMediaUri(String photoName, Integer maxWidthPx, Integer maxHeightPx) {
		StringBuilder sb = new StringBuilder()
			.append("/v1/")
			.append(photoName)
			.append("/media")
			.append("?skipHttpRedirect=true");

		if (maxWidthPx != null) {
			sb.append("&maxWidthPx=").append(maxWidthPx);
		}
		if (maxHeightPx != null) {
			sb.append("&maxHeightPx=").append(maxHeightPx);
		}
		return sb.toString();
	}

	private String normalizePhotoName(String photoName) {
		if (!StringUtils.hasText(photoName)) {
			return null;
		}

		return photoName.trim();
	}

	private Integer validatePhotoSize(int size) {
		if (size < PHOTO_SIZE_MIN || size > PHOTO_SIZE_MAX) {
			throw new IllegalArgumentException("photo size must be between 1 and 4800");
		}
		return size;
	}

	private record GooglePlaceIdResponse(
		List<PlaceItem> places
	) {
		private record PlaceItem(String id) {
		}
	}

	private record PhotoMediaResponse(@JsonProperty("photoUri") String photoUri) {
	}
}
