package waypoint.mvp.place.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.domain.content.ContentSnapshot;
import waypoint.mvp.place.domain.content.YouTubeContentSnapshot;
import waypoint.mvp.place.error.SocialMediaError;

class SocialMediaTest {

	private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=12345";

	@Test
	@DisplayName("올바른 URL로 생성하면 초기 상태는 PENDING이어야 한다.")
	void create_success() {
		// when
		SocialMedia socialMedia = SocialMedia.create(YOUTUBE_URL);

		// then
		assertThat(socialMedia.getStatus()).isEqualTo(SocialMediaStatus.PENDING);
		assertThat(socialMedia.getUrl()).isEqualTo(YOUTUBE_URL);
		assertThat(socialMedia.getType()).isNotNull();
		assertThat(socialMedia.getSummary()).isNull();
		assertThat(socialMedia.getSnapshot()).isNull();
	}

	@Test
	@DisplayName("지원하지 않는 URL로 생성하면 예외가 발생한다.")
	void create_fail_unsupported_url() {
		// given
		String invalidUrl = "https://www.naver.com";

		// when & then
		assertThatThrownBy(() -> SocialMedia.create(invalidUrl))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getBody().getProperties().get("code"))
			.isEqualTo(SocialMediaError.SOCIAL_MEDIA_UNSUPPORTED.name());
	}

	@Test
	@DisplayName("추출 작업을 시작하면 상태가 EXTRACTING으로 변경된다.")
	void startExtraction_success() {
		// given
		SocialMedia socialMedia = SocialMedia.create(YOUTUBE_URL);

		// when
		socialMedia.startExtraction();

		// then
		assertThat(socialMedia.getStatus()).isEqualTo(SocialMediaStatus.EXTRACTING);
	}

	@Test
	@DisplayName("PENDING 상태가 아닐 때 추출 작업을 시작하면 예외가 발생한다.")
	void startExtraction_fail_invalid_status() {
		// given
		SocialMedia socialMedia = SocialMedia.create(YOUTUBE_URL);
		socialMedia.startExtraction();

		// when & then
		assertThatThrownBy(socialMedia::startExtraction)
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getBody().getProperties().get("code"))
			.isEqualTo(SocialMediaError.SOCIAL_MEDIA_INVALID_STATUS.name());
	}

	@Test
	@DisplayName("추출 작업 완료 시 결과가 저장되고 상태가 SEARCHING으로 변경된다.")
	void startSearching_success() {
		// given
		SocialMedia socialMedia = SocialMedia.create(YOUTUBE_URL);
		socialMedia.startExtraction();

		String summary = "요약 내용";
		ContentSnapshot snapshot = YouTubeContentSnapshot.builder()
			.contentId("videoId")
			.build();

		// when
		socialMedia.completeExtraction(summary, snapshot);

		// then
		assertThat(socialMedia.getStatus()).isEqualTo(SocialMediaStatus.SEARCHING);
		assertThat(socialMedia.getSummary()).isEqualTo(summary);
		assertThat(socialMedia.getSnapshot()).isEqualTo(snapshot);
	}

	@Test
	@DisplayName("EXTRACTING 상태가 아닐 때 추출 작업을 완료하면 예외가 발생한다.")
	void startSearching_fail_invalid_status() {
		// given
		SocialMedia socialMedia = SocialMedia.create(YOUTUBE_URL);

		String summary = "요약 내용";
		ContentSnapshot snapshot = YouTubeContentSnapshot.builder()
			.contentId("videoId")
			.build();

		// when & then
		assertThatThrownBy(() -> socialMedia.completeExtraction(summary, snapshot))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getBody().getProperties().get("code"))
			.isEqualTo(SocialMediaError.SOCIAL_MEDIA_INVALID_STATUS.name());
	}

	@Test
	@DisplayName("작업 실패 처리를 하면 상태가 FAILED로 변경되고 실패 코드가 저장된다.")
	void fail_success() {
		// given
		SocialMedia socialMedia = SocialMedia.create(YOUTUBE_URL);
		socialMedia.startExtraction();

		// when
		socialMedia.fail(ExtractFailureCode.CONTENT_NOT_FOUND);

		// then
		assertThat(socialMedia.getStatus()).isEqualTo(SocialMediaStatus.FAILED);
		assertThat(socialMedia.getFailureCode()).isEqualTo(ExtractFailureCode.CONTENT_NOT_FOUND);
	}

	@Test
	@DisplayName("EXTRACTING 상태가 아닐 때 작업 실패 처리를 하면 예외가 발생한다.")
	void fail_fail_invalid_status() {
		// given
		SocialMedia socialMedia = SocialMedia.create(YOUTUBE_URL);

		// when & then
		assertThatThrownBy(() -> socialMedia.fail(ExtractFailureCode.CONTENT_NOT_FOUND))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getBody().getProperties().get("code"))
			.isEqualTo(SocialMediaError.SOCIAL_MEDIA_INVALID_STATUS.name());
	}
}
