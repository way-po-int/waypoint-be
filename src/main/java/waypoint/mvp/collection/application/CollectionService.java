package waypoint.mvp.collection.application;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.application.dto.request.CollectionCreateRequest;
import waypoint.mvp.collection.application.dto.request.CollectionUpdateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionMemberResponse;
import waypoint.mvp.collection.application.dto.response.CollectionResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionRole;
import waypoint.mvp.collection.domain.event.CollectionCreatedEvent;
import waypoint.mvp.collection.domain.service.CollectionAuthorizer;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.sharelink.application.dto.response.ShareLinkResponse;
import waypoint.mvp.sharelink.domain.ShareLink;
import waypoint.mvp.sharelink.error.ShareLinkError;
import waypoint.mvp.sharelink.infrastructure.ShareLinkRepository;
import waypoint.mvp.user.application.UserFinder;
import waypoint.mvp.user.domain.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionService {

	private final CollectionRepository collectionRepository;
	private final CollectionMemberService collectionMemberService;
	private final ApplicationEventPublisher eventPublisher;
	private final ShareLinkRepository shareLinkRepository;
	private final UserFinder userFinder;
	private final CollectionAuthorizer collectionAuthorizer;

	@Value("${waypoint.invitation.expiration-hours}")
	private long invitationExpirationHours;

	@Transactional
	public CollectionResponse createCollection(CollectionCreateRequest request, UserPrincipal user) {
		Collection collection = Collection.create(request.title());
		collectionRepository.save(collection);

		eventPublisher.publishEvent(
			CollectionCreatedEvent.of(collection.getId(), user)); // 이벤트는 실제 유저만 발생시키므로 캐스팅

		return CollectionResponse.from(collection);
	}

	public Page<CollectionResponse> findCollections(UserPrincipal user, Pageable pageable) {
		return collectionRepository.findAllByUserId(user.id(), pageable)
			.map(CollectionResponse::from);
	}

	/** Guest or Member 사용 가능한 메서드 */
	public CollectionResponse findCollectionById(Long collectionId, AuthPrincipal user) {
		collectionAuthorizer.verifyAccess(user, collectionId);
		Collection collection = getCollection(collectionId);

		return CollectionResponse.from(collection);
	}

	@Transactional
	public CollectionResponse updateCollection(Long collectionId, CollectionUpdateRequest request, UserPrincipal user) {
		collectionAuthorizer.verifyMember(user, collectionId);
		Collection collection = getCollection(collectionId);

		collection.update(request.title());

		return CollectionResponse.from(collection);
	}

	@Transactional
	public void changeOwner(Long collectionId, Long memberId, UserPrincipal user) {
		collectionAuthorizer.verifyOwner(user, collectionId);

		CollectionMember currentOwner = collectionMemberService.getMemberByUserId(collectionId, user.id());
		CollectionMember newOwner = collectionMemberService.getMember(collectionId, memberId);

		newOwner.updateRole(CollectionRole.OWNER);
		currentOwner.updateRole(CollectionRole.MEMBER);
	}

	public List<CollectionMemberResponse> getCollectionMembers(Long collectionId, AuthPrincipal user) {
		collectionAuthorizer.verifyAccess(user, collectionId);

		return collectionMemberService.getMembers(collectionId)
			.stream()
			.map(CollectionMemberResponse::from)
			.toList();
	}

	@Transactional
	public void withdrawCollectionMember(Long collectionId, UserPrincipal user) {
		collectionMemberService.withdraw(collectionId, user);
	}

	@Transactional
	public void expelCollectionMember(Long collectionId, Long memberId, UserPrincipal user) {
		collectionMemberService.expel(collectionId, memberId, user);
	}

	@Transactional
	public void deleteCollection(Long collectionId, UserPrincipal user) {
		collectionAuthorizer.verifyOwner(user, collectionId);
		Collection collection = getCollection(collectionId);

		collectionRepository.delete(collection);
	}

	@Transactional
	public ShareLinkResponse createInvitation(Long collectionId, UserPrincipal user) {
		collectionAuthorizer.verifyMember(user, collectionId);

		ShareLink shareLink = ShareLink.create(ShareLink.ShareLinkType.COLLECTION, collectionId, user.getId(),
			invitationExpirationHours);

		shareLinkRepository.save(shareLink);

		return ShareLinkResponse.from(shareLink);
	}

	@Transactional
	public Long addMemberFromShareLink(ShareLink shareLink, Long inviteeUserId) {
		if (shareLink.getTargetType() != ShareLink.ShareLinkType.COLLECTION) {
			throw new BusinessException(ShareLinkError.INVALID_INVITATION_LINK);
		}

		User inviteeUser = userFinder.findById(inviteeUserId);
		Collection collection = getCollection(shareLink.getTargetId());
		collectionMemberService.addMember(collection, inviteeUser);

		shareLink.increaseUseCount();

		return collection.getId();
	}

	private Collection getCollection(Long collectionId) {
		return collectionRepository.findById(collectionId)
			.orElseThrow(() -> new BusinessException(CollectionError.COLLECTION_NOT_FOUND));
	}

}
