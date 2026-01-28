package waypoint.mvp.global.auth.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;

@Aspect
@Component
@RequiredArgsConstructor
public class ResourceAuthorizationAspect {

	@Before("@annotation(authorize)")
	public void checkAuthorization(Authorize authorize) {
		AuthPrincipal user = getUserPrincipal();

		// 리소스 ID가 필요 없는 레벨 처리
		if (authorize.level() == AuthLevel.AUTHENTICATED && user.isGuest()) {
			{
				throw new AccessDeniedException("로그인한 사용자만 접근 가능합니다.");
			}
		}

	}

	private AuthPrincipal getUserPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return (AuthPrincipal)authentication.getPrincipal();
	}

}
