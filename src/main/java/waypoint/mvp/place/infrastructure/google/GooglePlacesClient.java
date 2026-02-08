package waypoint.mvp.place.infrastructure.google;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
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

	public Optional<String> searchTop1PlaceId(String textQuery) {
		if (!StringUtils.hasText(textQuery)) {
			return Optional.empty();
		}

		var response = restClient.post()
			.uri(textSearchPath)
			.header("X-Goog-FieldMask", TEXT_SEARCH_FIELD_MASK)
			.body(Map.of(
				"textQuery", textQuery,
				"pageSize", 1,
				"languageCode", "ko",
				"regionCode", "KR"
			))
			.retrieve()
			.onStatus(HttpStatusCode::isError, (req, res) -> {
				throw new PlaceSearchException(SearchFailureCode.PLACES_API_ERROR);
			})
			.body(GooglePlaceIdResponse.class);

		if (ObjectUtils.isEmpty(response) || ObjectUtils.isEmpty(response.places())) {
			return Optional.empty();
		}

		var placeItem = response.places().getFirst();
		return Optional.of(placeItem.id());
	}

	public Optional<GooglePlaceDetailsDto> getPlaceDetails(String placeId) {
		if (!StringUtils.hasText(placeId)) {
			return Optional.empty();
		}

		var response = restClient.get()
			.uri(detailsPath + "?languageCode=ko&regionCode=KR", placeId)
			.header("X-Goog-FieldMask", DETAILS_FIELD_MASK)
			.retrieve()
			.onStatus(HttpStatusCode::isError, (req, res) -> {
				throw new PlaceSearchException(SearchFailureCode.PLACES_API_ERROR);
			}).body(GooglePlaceDetailsDto.class);

		if (ObjectUtils.isEmpty(response)) {
			return Optional.empty();
		}

		return Optional.of(response);
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
		String n = photoName.trim();

		if (n.endsWith("/media")) {
			n = n.substring(0, n.length() - "/media".length());
		}
		if (n.startsWith("/v1/")) {
			n = n.substring("/v1/".length());
		}
		if (n.startsWith("v1/")) {
			n = n.substring("v1/".length());
		}
		if (n.startsWith("/")) {
			n = n.substring(1);
		}

		return StringUtils.hasText(n) ? n : null;
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
