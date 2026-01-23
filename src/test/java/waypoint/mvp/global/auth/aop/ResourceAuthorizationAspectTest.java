package waypoint.mvp.global.auth.aop;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import waypoint.mvp.auth.security.principal.GuestPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.auth.security.principal.WayPointUser;
import waypoint.mvp.collection.domain.service.CollectionAuthorizer;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;

@SpringBootTest(classes = {ResourceAuthorizationAspect.class, ResourceAuthorizationAspectTest.TestController.class})
@EnableAspectJAutoProxy
class ResourceAuthorizationAspectTest {

	@Autowired
	private TestController testController;

	@MockitoBean
	private CollectionAuthorizer collectionAuthorizer;

	private final Long resourceId = 1L;
	private final Long userId = 100L;

	// 테스트용 컨트롤러
	@Component
	static class TestController {
		@Authorize(level = AuthLevel.AUTHENTICATED)
		public void needsAuthenticated() {
		}

		@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
		public void needsGuestOrMember(Long id) {
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

		@Test
		@DisplayName("Authorizer가 예외를 던지면, Aspect도 예외를 그대로 전파한다.")
		void aspect_propagatesException() {
			// given
			setAuthentication(new UserPrincipal(userId));
			doThrow(new AccessDeniedException("접근 거부")).when(collectionAuthorizer)
				.verifyAccess(any(WayPointUser.class), eq(resourceId));

			// when & then
			assertThatThrownBy(() -> testController.needsGuestOrMember(resourceId))
				.isInstanceOf(AccessDeniedException.class);
		}
	}

	@Nested
	@DisplayName("AuthLevel에 따른 분기 처리 검증")
	class AuthLevelProcessingVerification {

		@Test
		@DisplayName("GUEST_OR_MEMBER 레벨은 CollectionAuthorizer.verifyAccess를 호출한다")
		void guestOrMember_callsVerifyAccess() {
			// given
			setAuthentication(new UserPrincipal(userId));
			ArgumentCaptor<WayPointUser> userCaptor = ArgumentCaptor.forClass(WayPointUser.class);
			ArgumentCaptor<Long> resourceIdCaptor = ArgumentCaptor.forClass(Long.class);

			// when
			testController.needsGuestOrMember(resourceId);

			// then
			verify(collectionAuthorizer, times(1)).verifyAccess(userCaptor.capture(), resourceIdCaptor.capture());
			assertThat(userCaptor.getValue().getId()).isEqualTo(userId);
			assertThat(resourceIdCaptor.getValue()).isEqualTo(resourceId);
		}
	}

	private void setAuthentication(WayPointUser user) {
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(user, "", null)
		);
	}
}

