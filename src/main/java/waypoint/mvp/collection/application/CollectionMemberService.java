package waypoint.mvp.collection.application;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionRole;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.error.CollectionMemberError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.user.domain.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionMemberService {

	private final CollectionMemberRepository collectionMemberRepository;
	private final CollectionRepository collectionRepository;
	private final ResourceAuthorizer collectionAuthorizer;

	public boolean isSameMember(CollectionMember member, CollectionMember other) {
		if (member == null || other == null) {
			return false;
		}
		return Objects.equals(member.getId(), other.getId());
	}

	@Transactional
	public void createInitialOwner(Collection collection, User user) {
		CollectionMember collectionMember = CollectionMember.create(collection, user, CollectionRole.OWNER);
		collectionMemberRepository.save(collectionMember);
	}

	@Transactional
	public void addMember(Collection collection, User user) {
		Optional<CollectionMember> withdrawnMemberOpt = collectionMemberRepository.findWithdrawnMember(
			collection.getId(), user.getId());

		if (withdrawnMemberOpt.isPresent()) {
			CollectionMember rejoinedMember = withdrawnMemberOpt.get();
			rejoinedMember.rejoin();
			rejoinedMember.updateProfile(user.getNickname(), user.getPicture());
		} else {
			collectionAuthorizer.checkIfMemberExists(collection.getId(), user.getId());
			CollectionMember newMember = CollectionMember.create(collection, user, CollectionRole.MEMBER);
			collectionMemberRepository.save(newMember);
		}
		collection.increaseMemberCount();
	}

	public CollectionMember findMember(Long collectionId, Long memberId) {

		return collectionMemberRepository.findActive(collectionId, memberId).orElseThrow(
			() -> new BusinessException(CollectionMemberError.MEMBER_NOT_FOUND)
		);
	}

	public CollectionMember findMember(Long collectionId, String memberExternalId) {

		return collectionMemberRepository.findActiveByMemberExternalId(collectionId, memberExternalId).orElseThrow(
			() -> new BusinessException(CollectionMemberError.MEMBER_NOT_FOUND)
		);
	}

	public CollectionMember findMemberByUserId(Long collectionId, Long userId) {

		return collectionMemberRepository.findActiveByUserId(collectionId, userId).orElseThrow(
			() -> new BusinessException(CollectionMemberError.MEMBER_NOT_FOUND)
		);
	}

	public List<CollectionMember> findMembers(Long collectionId) {
		return collectionMemberRepository.findActiveAll(collectionId);
	}

	@Transactional
	public void withdraw(Long collectionId, UserPrincipal user) {
		CollectionMember member = findMemberByUserId(collectionId, user.id());
		remove(member);
	}

	@Transactional
	public void expel(Long collectionId, String memberId, UserPrincipal user) {
		collectionAuthorizer.verifyOwner(user, collectionId);
		CollectionMember member = findMember(collectionId, memberId);
		remove(member);
	}

	private void remove(CollectionMember member) {
		Collection collection = member.getCollection();
		if (member.isOwner()) {
			throw new BusinessException(CollectionError.NEED_TO_DELEGATE_OWNERSHIP);
		} else {
			member.withdraw();
			collection.decreaseMemberCount();
		}
	}

}
