package waypoint.mvp.global.auth.aop;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.GuestPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;

@SpringBootTest(classes = {ResourceAuthorizationAspect.class, ResourceAuthorizationAspectTest.TestController.class})
@EnableAspectJAutoProxy
class ResourceAuthorizationAspectTest {

	@Autowired
	private TestController testController;

	private final Long userId = 100L;

	// 테스트용 컨트롤러
	@Component
	static class TestController {
		@Authorize(level = AuthLevel.AUTHENTICATED)
		public void needsAuthenticated() {
		}

	}

	@BeforeEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Nested
	@DisplayName("기본 인증/인가 검증")
	class BasicAuthorization {

		@Test
		@DisplayName("성공: 로그인 사용자는 AUTHENTICATED 레벨에 접근할 수 있다.")
		void authenticated_success_whenLoggedIn() {
			// given
			setAuthentication(new UserPrincipal(userId));

			// when & then
			assertThatCode(() -> testController.needsAuthenticated())
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("실패: Guest는 AUTHENTICATED 레벨에 접근할 수 없다.")
		void authenticated_fail_whenGuest() {
			// given
			setAuthentication(new GuestPrincipal("guest-code", ShareLinkType.COLLECTION, 1L));

			// when & then
			assertThatThrownBy(() -> testController.needsAuthenticated())
				.isInstanceOf(AccessDeniedException.class);
		}

	}

	private void setAuthentication(AuthPrincipal user) {
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(user, "", null)
		);
	}
}

