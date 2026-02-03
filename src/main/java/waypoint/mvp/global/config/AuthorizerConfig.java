package waypoint.mvp.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.error.ErrorCode;
import waypoint.mvp.plan.error.PlanError;
import waypoint.mvp.plan.infrastructure.persistence.PlanMemberRepository;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;

@Configuration
public class AuthorizerConfig {

	public record AuthorizerErrorCodes(
		ErrorCode notOwner,
		ErrorCode notMember,
		ErrorCode alreadyExists,
		ErrorCode notGuest
	) {
		public AuthorizerErrorCodes { // 파라미터 힌트를 위햇 사용
		}
	}

	@Bean
	public ResourceAuthorizer collectionAuthorizer(CollectionMemberRepository repository) {
		return new ResourceAuthorizer(
			repository::findActiveByUserId,
			repository::existsActive,
			ShareLinkType.COLLECTION,
			new AuthorizerErrorCodes(
				CollectionError.FORBIDDEN_NOT_OWNER,
				CollectionError.FORBIDDEN_NOT_MEMBER,
				CollectionError.MEMBER_ALREADY_EXISTS,
				CollectionError.FORBIDDEN_NOT_GUEST
			)
		);
	}

	@Bean
	public ResourceAuthorizer planAuthorizer(PlanMemberRepository repository) {
		return new ResourceAuthorizer(
			repository::findActiveByUserId,
			repository::existsActive,
			ShareLinkType.PLAN,
			new AuthorizerErrorCodes(
				PlanError.FORBIDDEN_NOT_OWNER,
				PlanError.FORBIDDEN_NOT_MEMBER,
				PlanError.MEMBER_ALREADY_EXISTS,
				PlanError.FORBIDDEN_NOT_GUEST
			)
		);
	}

}
