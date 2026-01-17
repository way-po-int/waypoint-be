package waypoint.mvp.collection.domain.service;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.user.domain.User;

@Component
@RequiredArgsConstructor
public class CollectionAuthorizer {

	private final CollectionMemberRepository memberRepository;

	public void verifyOwner(Collection collection, User user) {
		memberRepository.findByCollectionAndUser(collection, user)
			.filter(CollectionMember::isOwner)
			.orElseThrow(() -> new BusinessException(CollectionError.FORBIDDEN_NOT_OWNER));
	}

	public void verifyMember(Collection collection, User user) {
		if (!memberRepository.existsByCollectionAndUser(collection, user)) {
			throw new BusinessException(CollectionError.FORBIDDEN_NOT_MEMBER);
		}
	}

	public void checkIfMemberExists(Collection collection, User user) {
		if (memberRepository.existsByCollectionAndUser(collection, user)) {
			throw new BusinessException(CollectionError.MEMBER_ALREADY_EXISTS);
		}
	}
}
