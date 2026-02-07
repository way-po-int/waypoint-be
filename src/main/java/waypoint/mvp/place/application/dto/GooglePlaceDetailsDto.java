package waypoint.mvp.place.application.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GooglePlaceDetailsDto(
	@JsonProperty("id")
	String id,

	@JsonProperty("displayName")
	DisplayName displayName,

	@JsonProperty("formattedAddress")
	String formattedAddress,

	@JsonProperty("location")
	Location location,

	@JsonProperty("primaryType")
	String primaryType,

	@JsonProperty("googleMapsUri")
	String googleMapsUri,

	@JsonProperty("photos")
	List<Photo> photos
) {

	public record DisplayName(String text) {
	}

	public record Location(Double latitude, Double longitude) {
	}

	public record Photo(String name) {
	}

	public String getName() {
		return displayName != null ? displayName.text : null;
	}

	public String getFirstPhotoName() {
		return (photos != null && !photos.isEmpty()) ? photos.getFirst().name : null;
	}
}
