package waypoint.mvp.place.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.error.SocialMediaError;

class SocialMediaTypeTest {

	@ParameterizedTest
	@DisplayName("유효한 URL이 주어지면 올바른 타입을 반환한다")
	@CsvSource({
		"https://www.youtube.com/shorts/VideoId123,       YOUTUBE_SHORTS",
		"https://www.youtube.com/watch?v=VideoId123,      YOUTUBE",
		"https://youtu.be/VideoId123,                     YOUTUBE",
	})
	void from_success(String url, SocialMediaType expectedType) {
		// When
		SocialMediaType result = SocialMediaType.from(url);

		// Then
		assertThat(result).isEqualTo(expectedType);
	}

	@ParameterizedTest
	@DisplayName("지원하지 않는 도메인, 프로토콜, 형식이면 예외가 발생한다")
	@NullAndEmptySource
	@ValueSource(strings = {
		"https://google.com",
		"https://fake-youtube.com/watch?v=VideoId123",
		"https://youtube.com/invalid/path",
		"ftp://www.youtube.com/shorts/VideoId123",
		"not_a_url",
	})
	void from_fail_unsupported(String url) {
		// When & Then
		assertThatThrownBy(() -> SocialMediaType.from(url))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getBody().getProperties().get("code"))
			.isEqualTo(SocialMediaError.SOCIAL_MEDIA_UNSUPPORTED.name());
	}
}
