package waypoint.mvp.global.auth;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.global.common.Membership;
import waypoint.mvp.global.config.AuthorizerConfig;
import waypoint.mvp.global.error.ErrorCode;
import waypoint.mvp.global.error.exception.BusinessException;

/**
 * 리소스 권한 검증 공통 컴포넌트 (Strategy Pattern).
 * * <p>{@link AuthorizerConfig}에서 리소스별 로직을 주입받아 동작합니다.
 * <h3>주입 및 사용 방법:</h3>
 * <ul>
 * <li>동일한 타입의 빈이 여러 개 존재하므로, 주입 시 필드명을({@code collectionAuthorizer}, {@code planAuthorizer})과
 * 일치시키거나 {@code @Qualifier} 어노테이션을 사용해야 합니다.</li>
 */
@RequiredArgsConstructor
public final class ResourceAuthorizer {

	private final BiFunction<Long, Long, Optional<? extends Membership>> membershipFinder;
	private final BiPredicate<Long, Long> membershipExistsChecker;
	private final BiConsumer<AuthPrincipal, Long> guestVerifier;
	private final ErrorCode notOwnerError;
	private final ErrorCode notMemberError;
	private final ErrorCode alreadyExistsError;

	public void verifyAccess(AuthPrincipal user, Long resourceId) {
		if (user.isGuest()) {
			guestVerifier.accept(user, resourceId);
			return;
		}
		doVerifyMember(resourceId, user.getId());
	}

	public void verifyOwner(AuthPrincipal user, Long resourceId) {
		if (user.isGuest()) {
			throw new BusinessException(notOwnerError);
		}
		doVerifyOwner(resourceId, user.getId());
	}

	public void verifyMember(AuthPrincipal user, Long resourceId) {
		doVerifyMember(resourceId, user.getId());
	}

	public void checkIfMemberExists(Long resourceId, Long userId) {
		if (membershipExistsChecker.test(resourceId, userId)) {
			throw new BusinessException(alreadyExistsError);
		}
	}

	private void doVerifyOwner(Long resourceId, Long userId) {
		boolean isOwner = membershipFinder.apply(resourceId, userId)
			.map(Membership::isOwner)
			.orElse(false);

		if (!isOwner) {
			throw new BusinessException(notOwnerError);
		}
	}

	private void doVerifyMember(Long resourceId, Long userId) {
		if (!membershipExistsChecker.test(resourceId, userId)) {
			throw new BusinessException(notMemberError);
		}
	}
}
