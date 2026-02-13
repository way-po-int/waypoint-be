package waypoint.mvp.user.application.dto.response;

public record PresignedUrlResponse(String presignedUrl) {
	public static PresignedUrlResponse from(String url) {
		return new PresignedUrlResponse(url);
	}
}
