package waypoint.mvp.place.application.dto;

public record PointResponse(
	Double latitude,
	Double longitude
) {
	public static PointResponse of(Double latitude, Double longitude) {
		return new PointResponse(latitude, longitude);
	}
}
