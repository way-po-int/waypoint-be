package waypoint.mvp.global.auth;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.GuestPrincipal;
import waypoint.mvp.global.common.Membership;
import waypoint.mvp.global.config.AuthorizerConfig;
import waypoint.mvp.global.config.AuthorizerConfig.AuthorizerErrorCodes;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;

/**
 * 리소스 권한 검증 공통 컴포넌트 (Strategy Pattern).
 * * <p>{@link AuthorizerConfig}에서 리소스별 로직을 주입받아 동작합니다.
 * <h3>주입 및 사용 방법:</h3>
 * <ul>
 * <li>동일한 타입의 빈이 여러 개 존재하므로, 주입 시 필드명을({@code collectionAuthorizer}, {@code planAuthorizer})과
 * 일치시키거나 {@code @Qualifier} 어노테이션을 사용해야 합니다.</li>
 * </ul>
 */
public final class ResourceAuthorizer {

	private final BiFunction<Long, Long, Optional<? extends Membership>> membershipFinder;
	private final BiPredicate<Long, Long> membershipExistsChecker;
	private final ShareLinkType shareLinkType;
	private final AuthorizerErrorCodes authorizerErrorCodes;

	public ResourceAuthorizer(
		BiFunction<Long, Long, Optional<? extends Membership>> membershipFinder,
		BiPredicate<Long, Long> membershipExistsChecker,
		ShareLinkType shareLinkType,
		AuthorizerErrorCodes authorizerErrorCodes
	) {
		this.membershipFinder = membershipFinder;
		this.membershipExistsChecker = membershipExistsChecker;
		this.shareLinkType = shareLinkType;
		this.authorizerErrorCodes = authorizerErrorCodes;
	}

	public void verifyAccess(AuthPrincipal user, Long resourceId) {
		if (user.isGuest()) {
			verifyGuest(user, resourceId);
			return;
		}
		doVerifyMember(resourceId, user.getId());
	}

	public void verifyOwner(AuthPrincipal user, Long resourceId) {
		if (user.isGuest()) {
			throw new BusinessException(authorizerErrorCodes.notOwner());
		}
		doVerifyOwner(resourceId, user.getId());
	}

	public void verifyMember(AuthPrincipal user, Long resourceId) {
		if (user.isGuest()) {
			throw new BusinessException(authorizerErrorCodes.notMember());
		}
		doVerifyMember(resourceId, user.getId());
	}

	public void checkIfMemberExists(Long resourceId, Long userId) {
		if (membershipExistsChecker.test(resourceId, userId)) {
			throw new BusinessException(authorizerErrorCodes.alreadyExists());
		}
	}

	private void doVerifyOwner(Long resourceId, Long userId) {
		boolean isOwner = membershipFinder.apply(resourceId, userId)
			.map(Membership::isOwner)
			.orElse(false);

		if (!isOwner) {
			throw new BusinessException(authorizerErrorCodes.notOwner());
		}
	}

	private void doVerifyMember(Long resourceId, Long userId) {
		if (!membershipExistsChecker.test(resourceId, userId)) {
			throw new BusinessException(authorizerErrorCodes.notMember());
		}
	}

	private void verifyGuest(AuthPrincipal user, Long resourceId) {
		if (user instanceof GuestPrincipal guest) {
			guest.getTargetIdFor(shareLinkType)
				.filter(id -> id.equals(resourceId))
				.orElseThrow(() -> new BusinessException(authorizerErrorCodes.notGuest()));
		} else {
			throw new BusinessException(authorizerErrorCodes.notGuest());
		}
	}
}
