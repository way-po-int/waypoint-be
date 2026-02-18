package waypoint.mvp.user.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.user.error.UserError;

@ExtendWith(MockitoExtension.class)
class UserProfileImageServiceTest {

	private static final String BUCKET = "way-point-bucket";
	private static final String REGION = "ap-northeast-2";
	private static final String URL_FORMAT = "https://%s.s3.%s.amazonaws.com/%s";
	private static final String EXTERNAL_ID = "externalId123";

	@Mock
	private S3Presigner presigner;

	@Mock
	private S3Client s3Client;

	@Test
	@DisplayName("업로드 URL 생성 시 파일명을 UUID로 정규화하여 경로를 생성한다.")
	void presignProfileUpload_success_generatesUuidKeyAndUrls() throws Exception {
		// given
		UserProfileImageService service = new UserProfileImageService(
			presigner, s3Client, BUCKET, REGION, URL_FORMAT
		);

		PresignedPutObjectRequest presigned = mock(PresignedPutObjectRequest.class);
		given(presigned.url()).willReturn(URI.create("https://presigned.test/put").toURL());
		given(presigner.presignPutObject(any(PutObjectPresignRequest.class))).willReturn(presigned);

		ArgumentCaptor<PutObjectPresignRequest> captor =
			ArgumentCaptor.forClass(PutObjectPresignRequest.class);

		// when
		var result = service.presignProfileUpload(EXTERNAL_ID, "IMAGE/PNG");

		// then
		assertThat(result.presignedUrl()).isEqualTo("https://presigned.test/put");

		assertThat(result.pictureUrl()).contains("/users/" + EXTERNAL_ID + "/profile/");
		assertThat(result.pictureUrl()).endsWith(".png");

		String fileName = result.pictureUrl().substring(result.pictureUrl().lastIndexOf('/') + 1);
		String uuidPart = fileName.substring(0, fileName.length() - ".png".length());
		assertThatCode(() -> UUID.fromString(uuidPart)).doesNotThrowAnyException();

		then(presigner).should().presignPutObject(captor.capture());

		PutObjectPresignRequest presignRequest = captor.getValue();
		assertThat(presignRequest.signatureDuration()).isEqualTo(Duration.ofMinutes(10));

		var putReq = presignRequest.putObjectRequest();
		assertThat(putReq.bucket()).isEqualTo(BUCKET);
		assertThat(putReq.contentType()).isEqualTo("image/png");
		assertThat(putReq.cacheControl()).isEqualTo("public, max-age=31536000, immutable");

		assertThat(putReq.key()).startsWith("users/" + EXTERNAL_ID + "/profile/");
		assertThat(putReq.key()).endsWith(".png");
	}

	@Test
	@DisplayName("지원하지 않는 콘텐츠 타입이면 UNSUPPORTED_IMAGE_CONTENT_TYPE 예외가 발생한다.")
	void presignProfileUpload_fail_unsupportedContentType() {
		// given
		UserProfileImageService service = new UserProfileImageService(
			presigner, s3Client, BUCKET, REGION, URL_FORMAT
		);

		// when & then
		assertThatThrownBy(() -> service.presignProfileUpload(EXTERNAL_ID, "image/gif"))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getBody().getProperties().get("code"))
			.isEqualTo(UserError.UNSUPPORTED_IMAGE_CONTENT_TYPE.name());
	}

	@Test
	@DisplayName("콘텐츠 타입이 없으면 UNSUPPORTED_IMAGE_CONTENT_TYPE 예외가 발생한다.")
	void presignProfileUpload_fail_nullContentType() {
		// given
		UserProfileImageService service = new UserProfileImageService(
			presigner, s3Client, BUCKET, REGION, URL_FORMAT
		);

		// when & then
		assertThatThrownBy(() -> service.presignProfileUpload(EXTERNAL_ID, null))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getBody().getProperties().get("code"))
			.isEqualTo(UserError.UNSUPPORTED_IMAGE_CONTENT_TYPE.name());
	}

	@Test
	@DisplayName("관리 대상 경로의 URL인 경우 S3 오브젝트 삭제를 요청한다.")
	void deleteProfileImageIfManaged_success_callsDeleteObject() {
		// given
		UserProfileImageService service = new UserProfileImageService(
			presigner, s3Client, BUCKET, REGION, URL_FORMAT
		);

		String url = "https://way-point-bucket.s3.ap-northeast-2.amazonaws.com/users/"
			+ EXTERNAL_ID + "/profile/" + UUID.randomUUID() + ".png";

		ArgumentCaptor<DeleteObjectRequest> captor =
			ArgumentCaptor.forClass(DeleteObjectRequest.class);

		// when
		service.deleteProfileImageIfManaged(EXTERNAL_ID, url);

		// then
		then(s3Client).should().deleteObject(captor.capture());

		DeleteObjectRequest req = captor.getValue();
		assertThat(req.bucket()).isEqualTo(BUCKET);
		assertThat(req.key()).startsWith("users/" + EXTERNAL_ID + "/profile/");
		assertThat(req.key()).endsWith(".png");
	}

	@Test
	@DisplayName("레거시 고정 키 형식의 URL도 삭제를 시도한다.")
	void deleteProfileImageIfManaged_success_legacyFixedKey() {
		// given
		UserProfileImageService service = new UserProfileImageService(
			presigner, s3Client, BUCKET, REGION, URL_FORMAT
		);

		String url = "https://way-point-bucket.s3.ap-northeast-2.amazonaws.com/users/" + EXTERNAL_ID + "/profile";

		ArgumentCaptor<DeleteObjectRequest> captor =
			ArgumentCaptor.forClass(DeleteObjectRequest.class);

		// when
		service.deleteProfileImageIfManaged(EXTERNAL_ID, url);

		// then
		then(s3Client).should().deleteObject(captor.capture());
		assertThat(captor.getValue().key()).isEqualTo("users/" + EXTERNAL_ID + "/profile");
	}

	@Test
	@DisplayName("이미지 URL이 비어있으면 아무 작업도 하지 않는다.")
	void deleteProfileImageIfManaged_noop_whenBlank() {
		// given
		UserProfileImageService service = new UserProfileImageService(
			presigner, s3Client, BUCKET, REGION, URL_FORMAT
		);

		// when
		service.deleteProfileImageIfManaged(EXTERNAL_ID, null);
		service.deleteProfileImageIfManaged(EXTERNAL_ID, "");
		service.deleteProfileImageIfManaged(EXTERNAL_ID, "   ");

		// then
		then(s3Client).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("유효하지 않은 URL은 무시하고 삭제하지 않는다.")
	void deleteProfileImageIfManaged_noop_whenInvalidUrl() {
		// given
		UserProfileImageService service = new UserProfileImageService(
			presigner, s3Client, BUCKET, REGION, URL_FORMAT
		);

		// when
		service.deleteProfileImageIfManaged(EXTERNAL_ID, "not_a_url");

		// then
		then(s3Client).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("타인이나 잘못된 경로의 이미지는 삭제하지 않는다.")
	void deleteProfileImageIfManaged_noop_whenNotManagedPath() {
		// given
		UserProfileImageService service = new UserProfileImageService(
			presigner, s3Client, BUCKET, REGION, URL_FORMAT
		);

		String otherUser = "otherUser123";
		String url = "https://way-point-bucket.s3.ap-northeast-2.amazonaws.com/users/"
			+ otherUser + "/profile/" + UUID.randomUUID() + ".png";

		// when
		service.deleteProfileImageIfManaged(EXTERNAL_ID, url);

		// then
		then(s3Client).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Path-style URL은 관리 대상에서 제외하고 삭제하지 않는다.")
	void deleteProfileImageIfManaged_noop_whenPathStyleUrl() {
		// given
		UserProfileImageService service = new UserProfileImageService(
			presigner, s3Client, BUCKET, REGION, URL_FORMAT
		);

		String url = "https://s3.ap-northeast-2.amazonaws.com/" + BUCKET
			+ "/users/" + EXTERNAL_ID + "/profile/" + UUID.randomUUID() + ".png";

		// when
		service.deleteProfileImageIfManaged(EXTERNAL_ID, url);

		// then
		then(s3Client).shouldHaveNoInteractions();
	}
}
