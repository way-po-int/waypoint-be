package waypoint.mvp.user.application;

import java.time.Duration;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.user.error.UserError;

@Service
public class UserProfileImageService {

	private static final Duration PRESIGN_TTL = Duration.ofMinutes(10);
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"image/jpeg",
		"image/png",
		"image/webp",
		"image/jpg"
	);

	private final S3Presigner presigner;
	private final String bucket;
	private final String region;
	private final String urlFormat;

	public UserProfileImageService(
		S3Presigner presigner,
		@Value("${aws.s3.bucket}") String bucket,
		@Value("${aws.region}") String region,
		@Value("${aws.s3.url-format}") String urlFormat
	) {
		this.presigner = presigner;
		this.bucket = bucket;
		this.region = region;
		this.urlFormat = urlFormat;
	}

	public PresignedUpload presignProfileUpload(String userExternalId, String contentType) {
		String normalized = normalizeAndValidateContentType(contentType);

		String key = "users/%s/profile".formatted(userExternalId);

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.contentType(normalized)
			.cacheControl("no-cache")
			.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(PRESIGN_TTL)
			.putObjectRequest(putObjectRequest)
			.build();

		String presignedUrl = presigner.presignPutObject(presignRequest).url().toString();
		String pictureUrl = urlFormat.formatted(bucket, region, key);

		return new PresignedUpload(presignedUrl, pictureUrl);
	}

	private String normalizeAndValidateContentType(String contentType) {
		String normalized = contentType == null ? "" : contentType.trim().toLowerCase(java.util.Locale.ROOT);
		if (!ALLOWED_CONTENT_TYPES.contains(normalized)) {
			throw new BusinessException(UserError.UNSUPPORTED_IMAGE_CONTENT_TYPE);
		}
		return normalized;
	}

	public record PresignedUpload(String presignedUrl, String pictureUrl) {
	}
}
