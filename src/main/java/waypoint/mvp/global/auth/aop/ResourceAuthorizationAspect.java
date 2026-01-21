package waypoint.mvp.global.auth.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.WayPointUser;
import waypoint.mvp.collection.domain.service.CollectionAuthorizer;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;

@Aspect
@Component
@RequiredArgsConstructor
public class ResourceAuthorizationAspect {

	private final CollectionAuthorizer collectionAuthorizer;

	@Before("@annotation(authorize)")
	public void checkAuthorization(JoinPoint joinPoint, Authorize authorize) {
		WayPointUser user = getUserPrincipal();

		// 리소스 ID가 필요 없는 레벨 처리
		if (authorize.level() == AuthLevel.AUTHENTICATED) {
			if (user.isGuest()) {
				throw new AccessDeniedException("로그인한 사용자만 접근 가능합니다.");
			}
			return;
		}

		// 리소스 ID가 필요한 레벨 처리
		Long collectionId = findCollectionId(joinPoint);

		switch (authorize.level()) {
			case GUEST_OR_MEMBER -> collectionAuthorizer.verifyAccess(user, collectionId);
			case MEMBER -> collectionAuthorizer.verifyMember(user, collectionId);
			case OWNER -> collectionAuthorizer.verifyOwner(user, collectionId);
			default -> throw new IllegalStateException("처리할 수 없는 AuthLevel입니다: " + authorize.level());
		}
	}

	private WayPointUser getUserPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return (WayPointUser)authentication.getPrincipal();
	}

	private Long findCollectionId(JoinPoint joinPoint) {
		return Arrays.stream(joinPoint.getArgs())
			.filter(Long.class::isInstance)
			.map(Long.class::cast)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("@PathVariable에 Long 타입의 collectionId가 필요합니다."));
	}
}
