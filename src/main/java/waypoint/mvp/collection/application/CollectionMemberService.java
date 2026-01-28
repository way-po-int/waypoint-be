package waypoint.mvp.collection.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionRole;
import waypoint.mvp.collection.domain.service.CollectionAuthorizer;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.error.CollectionMemberError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.user.domain.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionMemberService {

	private final CollectionMemberRepository collectionMemberRepository;
	private final CollectionRepository collectionRepository;
	private final CollectionAuthorizer collectionAuthorizer;

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

	public CollectionMember getMember(Long collectionId, Long memberId) {

		return collectionMemberRepository.findActive(memberId, collectionId).orElseThrow(
			() -> new BusinessException(CollectionMemberError.MEMBER_NOT_FOUND)
		);
	}

	public CollectionMember getMemberByUserId(Long collectionId, Long userId) {

		return collectionMemberRepository.findActiveByUserId(collectionId, userId).orElseThrow(
			() -> new BusinessException(CollectionMemberError.MEMBER_NOT_FOUND)
		);
	}

	public List<CollectionMember> getMembers(Long collectionId) {
		return collectionMemberRepository.findActiveAll(collectionId);
	}

	@Transactional
	public void withdraw(Long collectionId, UserPrincipal user) {
		CollectionMember member = getMemberByUserId(collectionId, user.id());
		remove(collectionId, member);
	}

	@Transactional
	public void expel(Long collectionId, Long memberId, UserPrincipal user) {
		collectionAuthorizer.verifyOwner(user, collectionId);
		CollectionMember member = getMember(collectionId, memberId);
		remove(collectionId, member);
	}

	private void remove(Long collectionId, CollectionMember member) {
		Collection collection = collectionRepository.findById(collectionId)
			.orElseThrow(() -> new BusinessException(CollectionError.COLLECTION_NOT_FOUND));

		if (member.isOwner()) {
			throw new BusinessException(CollectionError.NEED_TO_DELEGATE_OWNERSHIP);
		} else {
			member.withdraw();
			collection.decreaseMemberCount();
		}
	}

}
