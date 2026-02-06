package waypoint.mvp.place.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.domain.content.ContentSnapshot;
import waypoint.mvp.place.domain.content.YouTubeContentSnapshot;
import waypoint.mvp.place.error.ExtractFailureCode;
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
	@DisplayName("SEARCHING 상태가 아닐 때 완료 처리를 하면 예외가 발생한다.")
	void complete_fail_invalid_status() {
		// given
		SocialMedia socialMedia = SocialMedia.create(YOUTUBE_URL);
		socialMedia.startExtraction();

		// when & then
		assertThatThrownBy(socialMedia::complete)
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getBody().getProperties().get("code"))
			.isEqualTo(SocialMediaError.SOCIAL_MEDIA_INVALID_STATUS.name());
	}

	@Test
	@DisplayName("재시도 불가능한 실패 코드로 실패 처리하면 상태가 FAILED로 변경된다.")
	void fail_non_retryable() {
		// given
		SocialMedia socialMedia = SocialMedia.create(YOUTUBE_URL);
		socialMedia.startExtraction();
		ExtractFailureCode nonRetryableCode = ExtractFailureCode.UNEXPECTED_ERROR;

		// when
		socialMedia.fail(nonRetryableCode);

		// then
		assertThat(socialMedia.getStatus()).isEqualTo(SocialMediaStatus.FAILED);
		assertThat(socialMedia.getFailureCode()).isEqualTo(nonRetryableCode);
	}

	@Test
	@DisplayName("재시도 가능한 실패 코드로 실패 처리하면 상태가 RETRY_WAITING으로 변경된다.")
	void fail_retryable() {
		// given
		SocialMedia socialMedia = SocialMedia.create(YOUTUBE_URL);
		socialMedia.startExtraction();
		ExtractFailureCode retryableCode = ExtractFailureCode.GENAI_ERROR;

		// when
		socialMedia.fail(retryableCode);

		// then
		assertThat(socialMedia.getStatus()).isEqualTo(SocialMediaStatus.RETRY_WAITING);
		assertThat(socialMedia.getFailureCode()).isEqualTo(retryableCode);
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

	@Test
	@DisplayName("장소 검색까지 모두 완료되면 상태를 COMPLETED로 변경한다.")
	void complete_success() {
		// given
		SocialMedia socialMedia = SocialMedia.create(YOUTUBE_URL);
		socialMedia.startExtraction();

		String summary = "요약 내용";
		ContentSnapshot snapshot = YouTubeContentSnapshot.builder()
			.contentId("videoId")
			.build();

		socialMedia.completeExtraction(summary, snapshot);

		// when
		socialMedia.complete();

		// then
		assertThat(socialMedia.getStatus()).isEqualTo(SocialMediaStatus.COMPLETED);
	}

	@Test
	@DisplayName("종료된 상태(COMPLETED, FAILED)면 isFinished는 true다.")
	void isFinished_true() {
		// 1. COMPLETED
		SocialMedia completed = SocialMedia.create(YOUTUBE_URL);
		completed.startExtraction();
		completed.completeExtraction("s", null);
		completed.complete();

		// 2. FAILED
		SocialMedia failed = SocialMedia.create(YOUTUBE_URL);
		failed.startExtraction();
		failed.fail(ExtractFailureCode.CONTENT_NOT_FOUND);

		assertThat(completed.isFinished()).isTrue();
		assertThat(failed.isFinished()).isTrue();
	}

	@Test
	@DisplayName("진행 중인 상태면 isFinished는 false다.")
	void isFinished_false() {
		SocialMedia pending = SocialMedia.create(YOUTUBE_URL);
		SocialMedia extracting = SocialMedia.create(YOUTUBE_URL);

		extracting.startExtraction();

		assertThat(pending.isFinished()).isFalse();
		assertThat(extracting.isFinished()).isFalse();
	}
}
