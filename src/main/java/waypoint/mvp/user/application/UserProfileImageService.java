package waypoint.mvp.user.application;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.user.error.UserError;

@Service
public class UserProfileImageService {

	private static final Duration PRESIGN_TTL = Duration.ofMinutes(10);

	private static final Map<String, String> CONTENT_TYPE_TO_EXTENSION = Map.of(
		"image/jpeg", ".jpg",
		"image/jpg", ".jpg",
		"image/png", ".png",
		"image/webp", ".webp"
	);

	private final S3Presigner presigner;
	private final S3Client s3Client;
	private final String bucket;
	private final String region;
	private final String urlFormat;

	public UserProfileImageService(
		S3Presigner presigner,
		S3Client s3Client,
		@Value("${aws.s3.bucket}") String bucket,
		@Value("${aws.region}") String region,
		@Value("${aws.s3.url-format}") String urlFormat
	) {
		this.presigner = presigner;
		this.s3Client = s3Client;
		this.bucket = bucket;
		this.region = region;
		this.urlFormat = urlFormat;
	}

	public PresignedUpload presignProfileUpload(String userExternalId, String contentType) {
		String normalized = normalizeContentType(contentType);

		String ext = CONTENT_TYPE_TO_EXTENSION.get(normalized);
		if (ext == null) {
			throw new BusinessException(UserError.UNSUPPORTED_IMAGE_CONTENT_TYPE);
		}

		String fileName = UUID.randomUUID() + ext;
		String key = "users/%s/profile/%s".formatted(userExternalId, fileName);

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.contentType(normalized)
			.cacheControl("public, max-age=31536000, immutable")
			.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(PRESIGN_TTL)
			.putObjectRequest(putObjectRequest)
			.build();

		String presignedUrl = presigner.presignPutObject(presignRequest).url().toString();
		String pictureUrl = urlFormat.formatted(bucket, region, key);

		return new PresignedUpload(presignedUrl, pictureUrl);
	}

	public void deleteProfileImageIfManaged(String userExternalId, String pictureUrl) {
		String key = extractManagedKey(userExternalId, pictureUrl);
		if (key == null) {
			return;
		}

		s3Client.deleteObject(DeleteObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.build());
	}

	private String extractManagedKey(String userExternalId, String pictureUrl) {
		if (pictureUrl == null || pictureUrl.isBlank()) {
			return null;
		}

		URI uri;
		try {
			uri = URI.create(pictureUrl);
		} catch (IllegalArgumentException e) {
			return null;
		}

		String path = uri.getPath();
		if (path == null || path.isBlank()) {
			return null;
		}
		String key = path.startsWith("/") ? path.substring(1) : path;

		String bucketPrefix = bucket + "/";
		if (key.startsWith(bucketPrefix)) {
			key = key.substring(bucketPrefix.length());
		}

		String base = "users/%s/profile".formatted(userExternalId);
		if (!(key.equals(base) || key.startsWith(base + "/"))) {
			return null;
		}
		return key;
	}

	private String normalizeContentType(String contentType) {
		return contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
	}

	public record PresignedUpload(String presignedUrl, String pictureUrl) {
	}
}
