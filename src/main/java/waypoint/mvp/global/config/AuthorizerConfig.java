package waypoint.mvp.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.GuestPrincipal;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.error.ErrorCode;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.error.PlanError;
import waypoint.mvp.plan.infrastructure.persistence.PlanMemberRepository;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;

@Configuration
public class AuthorizerConfig {

	@Bean
	public ResourceAuthorizer collectionAuthorizer(CollectionMemberRepository repository) {
		return new ResourceAuthorizer(
			repository::findActiveByUserId,
			repository::existsActive,
			(user, resourceId) -> verifyGuest(user, resourceId, ShareLinkType.COLLECTION,
				CollectionError.FORBIDDEN_NOT_GUEST),
			CollectionError.FORBIDDEN_NOT_OWNER,
			CollectionError.FORBIDDEN_NOT_MEMBER,
			CollectionError.MEMBER_ALREADY_EXISTS

		);
	}

	@Bean
	public ResourceAuthorizer planAuthorizer(PlanMemberRepository repository) {
		return new ResourceAuthorizer(
			repository::findActiveByUserId,
			repository::existsActive,
			(user, resourceId) -> verifyGuest(user, resourceId, ShareLinkType.PLAN,
				PlanError.FORBIDDEN_NOT_MEMBER),
			PlanError.FORBIDDEN_NOT_OWNER,
			PlanError.FORBIDDEN_NOT_MEMBER,
			PlanError.MEMBER_ALREADY_EXISTS
		);
	}

	private void verifyGuest(AuthPrincipal user, Long resourceId, ShareLinkType type, ErrorCode errorCode) {
		if (user instanceof GuestPrincipal guest) {
			guest.getTargetIdFor(type)
				.filter(id -> id.equals(resourceId))
				.orElseThrow(() -> new BusinessException(errorCode));
		} else {
			throw new BusinessException(errorCode);
		}
	}
}
