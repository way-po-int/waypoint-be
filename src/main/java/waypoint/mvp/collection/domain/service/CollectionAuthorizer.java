package waypoint.mvp.collection.domain.service;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.GuestPrincipal;
import waypoint.mvp.auth.security.principal.WayPointUser;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.sharelink.domain.ShareLink;

@Component
@RequiredArgsConstructor
public class CollectionAuthorizer {

	private final CollectionMemberRepository memberRepository;

	public void verifyAccess(WayPointUser user, Long collectionId) {
		if (user.isGuest()) {
			verifyGuestAccess(user, collectionId);
			return;
		}
		verifyMember(user, collectionId);
	}

	public void verifyOwner(WayPointUser user, Long collectionId) {
		if (user.isGuest() || !isOwner(collectionId, user.getId())) {
			throw new BusinessException(CollectionError.FORBIDDEN_NOT_OWNER);
		}
	}

	public void verifyMember(WayPointUser user, Long collectionId) {
		if (!memberRepository.existsByCollectionIdAndUserId(collectionId, user.getId())) {
			throw new BusinessException(CollectionError.FORBIDDEN_NOT_MEMBER);
		}
	}

	public void checkIfMemberExists(Long collectionId, Long userId) {
		if (memberRepository.existsByCollectionIdAndUserId(collectionId, userId)) {
			throw new BusinessException(CollectionError.MEMBER_ALREADY_EXISTS);
		}
	}

	private void verifyGuestAccess(WayPointUser user, Long collectionId) {
		if (user instanceof GuestPrincipal guest) {
			guest.getTargetIdFor(ShareLink.ShareLinkType.COLLECTION)
				.filter(id -> id.equals(collectionId))
				.orElseThrow(() -> new BusinessException(CollectionError.FORBIDDEN_NOT_GUEST));
		} else {
			throw new BusinessException(CollectionError.FORBIDDEN_NOT_GUEST);
		}
	}

	private boolean isOwner(Long collectionId, Long userId) {
		return memberRepository.findByCollectionIdAndUserId(collectionId, userId)
			.map(CollectionMember::isOwner)
			.orElse(false);
	}
}
