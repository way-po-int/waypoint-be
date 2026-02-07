package waypoint.mvp.place.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.error.SearchFailureCode;
import waypoint.mvp.place.error.SocialMediaError;

@ExtendWith(MockitoExtension.class)
class SocialMediaPlaceTest {

	private static final String SEARCH_QUERY = "부산 맛집";

	@Mock
	private SocialMedia socialMedia;

	@Mock
	private Place place;

	@Test
	@DisplayName("생성 시 초기 상태는 PENDING이어야 한다.")
	void create_success() {
		// when
		SocialMediaPlace socialMediaPlace = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);

		// then
		assertThat(socialMediaPlace.getStatus()).isEqualTo(PlaceSearchStatus.PENDING);
		assertThat(socialMediaPlace.getSearchQuery()).isEqualTo(SEARCH_QUERY);
		assertThat(socialMediaPlace.getSocialMedia()).isEqualTo(socialMedia);
		assertThat(socialMediaPlace.getPlace()).isNull();
	}

	@Test
	@DisplayName("처리 작업을 시작하면 상태가 PROCESSING으로 변경된다.")
	void process_success() {
		// given
		SocialMediaPlace socialMediaPlace = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);

		// when
		socialMediaPlace.process();

		// then
		assertThat(socialMediaPlace.getStatus()).isEqualTo(PlaceSearchStatus.PROCESSING);
	}

	@Test
	@DisplayName("PENDING 상태가 아닐 때 처리를 시작하면 예외가 발생한다.")
	void process_fail_invalid_status() {
		// given
		SocialMediaPlace socialMediaPlace = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);
		socialMediaPlace.process(); // PROCESSING 상태

		// when & then
		assertThatThrownBy(socialMediaPlace::process)
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getBody().getProperties().get("code"))
			.isEqualTo(SocialMediaError.SOCIAL_MEDIA_INVALID_STATUS.name());
	}

	@Test
	@DisplayName("작업 완료 시 장소가 연결되고 상태가 COMPLETED로 변경된다.")
	void complete_success() {
		// given
		SocialMediaPlace socialMediaPlace = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);
		socialMediaPlace.process();

		// when
		socialMediaPlace.complete(place);

		// then
		assertThat(socialMediaPlace.getStatus()).isEqualTo(PlaceSearchStatus.COMPLETED);
		assertThat(socialMediaPlace.getPlace()).isEqualTo(place);
	}

	@Test
	@DisplayName("PROCESSING 상태가 아닐 때 완료 처리를 하면 예외가 발생한다.")
	void complete_fail_invalid_status() {
		// given
		SocialMediaPlace socialMediaPlace = SocialMediaPlace.create(socialMedia, SEARCH_QUERY); // PENDING 상태

		// when & then
		assertThatThrownBy(() -> socialMediaPlace.complete(place))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getBody().getProperties().get("code"))
			.isEqualTo(SocialMediaError.SOCIAL_MEDIA_INVALID_STATUS.name());
	}

	@Test
	@DisplayName("검색 결과가 없을 경우 상태가 NOT_FOUND로 변경된다.")
	void notFound_success() {
		// given
		SocialMediaPlace socialMediaPlace = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);
		socialMediaPlace.process();

		// when
		socialMediaPlace.notFound();

		// then
		assertThat(socialMediaPlace.getStatus()).isEqualTo(PlaceSearchStatus.NOT_FOUND);
	}

	@Test
	@DisplayName("재시도 불가능한 코드로 실패 처리하면 상태가 FAILED로 변경된다.")
	void fail_non_retryable() {
		// given
		SocialMediaPlace socialMediaPlace = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);
		socialMediaPlace.process();
		SearchFailureCode nonRetryableCode = SearchFailureCode.UNEXPECTED_ERROR;

		// when
		socialMediaPlace.fail(nonRetryableCode);

		// then
		assertThat(socialMediaPlace.getStatus()).isEqualTo(PlaceSearchStatus.FAILED);
		assertThat(socialMediaPlace.getFailureCode()).isEqualTo(nonRetryableCode);
	}

	@Test
	@DisplayName("재시도 가능한 코드로 실패 처리하면 상태가 RETRY_WAITING으로 변경된다.")
	void fail_retryable() {
		// given
		SocialMediaPlace socialMediaPlace = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);
		socialMediaPlace.process();
		SearchFailureCode retryableCode = SearchFailureCode.PLACES_API_ERROR;

		// when
		socialMediaPlace.fail(retryableCode);

		// then
		assertThat(socialMediaPlace.getStatus()).isEqualTo(PlaceSearchStatus.RETRY_WAITING);
		assertThat(socialMediaPlace.getFailureCode()).isEqualTo(retryableCode);
	}

	@Test
	@DisplayName("종료된 상태(COMPLETED, FAILED, NOT_FOUND)면 isFinished는 true를 반환한다.")
	void isFinished_true() {
		// 1. COMPLETED
		SocialMediaPlace completed = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);
		completed.process();
		completed.complete(place);

		// 2. FAILED
		SocialMediaPlace failed = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);
		failed.process();
		failed.fail(SearchFailureCode.UNEXPECTED_ERROR);

		// 3. NOT_FOUND
		SocialMediaPlace notFound = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);
		notFound.process();
		notFound.notFound();

		assertThat(completed.isFinished()).isTrue();
		assertThat(failed.isFinished()).isTrue();
		assertThat(notFound.isFinished()).isTrue();
	}

	@Test
	@DisplayName("진행 중인 상태(PENDING, PROCESSING, RETRY_WAITING)면 isFinished는 false를 반환한다.")
	void isFinished_false() {
		// 1. PENDING
		SocialMediaPlace pending = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);

		// 2. PROCESSING
		SocialMediaPlace processing = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);
		processing.process();

		// 3. RETRY_WAITING
		SocialMediaPlace retryWaiting = SocialMediaPlace.create(socialMedia, SEARCH_QUERY);
		retryWaiting.process();
		retryWaiting.fail(SearchFailureCode.PLACES_API_ERROR);

		assertThat(pending.isFinished()).isFalse();
		assertThat(processing.isFinished()).isFalse();
		assertThat(retryWaiting.isFinished()).isFalse();
	}
}
