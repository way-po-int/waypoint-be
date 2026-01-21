package waypoint.mvp.collection.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserInfo;
import waypoint.mvp.auth.security.principal.WayPointUser;
import waypoint.mvp.collection.application.dto.request.CollectionCreateRequest;
import waypoint.mvp.collection.application.dto.request.CollectionUpdateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionRole;
import waypoint.mvp.collection.domain.event.CollectionCreatedEvent;
import waypoint.mvp.collection.domain.service.CollectionAuthorizer;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
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
	private final CollectionMemberRepository collectionMemberRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final ShareLinkRepository shareLinkRepository;
	private final UserFinder userFinder;
	private final CollectionAuthorizer collectionAuthorizer;

	@Value("${waypoint.invitation.expiration-hours}")
	private long invitationExpirationHours;

	@Transactional
	public CollectionResponse createCollection(CollectionCreateRequest request, WayPointUser user) {
		Collection collection = Collection.create(request.title());
		collectionRepository.save(collection);

		eventPublisher.publishEvent(
			CollectionCreatedEvent.of(collection.getId(), (UserInfo)user)); // 이벤트는 실제 유저만 발생시키므로 캐스팅

		return CollectionResponse.from(collection);
	}

	public Page<CollectionResponse> findCollections(UserInfo user, Pageable pageable) {
		return collectionRepository.findAllByUserId(user.id(), pageable)
			.map(CollectionResponse::from);
	}

	/** TODO findCollectionById()는 '인증 주체 타입 판별'도 하는 중 책임 분리 필요, 컬렉션 조회만할 수 있도록 해야함 **/
	public CollectionResponse findCollectionById(Long collectionId, WayPointUser wayPointUser) {
		Collection collection = getCollection(collectionId);

		if (wayPointUser.isGuest()) {
			wayPointUser.getTargetIdFor(ShareLink.ShareLinkType.COLLECTION)
				.filter(id -> id.equals(collectionId))
				.orElseThrow(() -> new BusinessException(CollectionError.FORBIDDEN_NOT_GUEST));
		} else {
			User user = userFinder.findById(wayPointUser.getId());
			collectionAuthorizer.verifyMember(collection, user);
		}

		return CollectionResponse.from(collection);
	}

	@Transactional
	public CollectionResponse updateCollection(Long collectionId, CollectionUpdateRequest request, UserInfo user) {
		Collection collection = getCollection(collectionId);

		if (user.isGuest()) {
			throw new BusinessException(CollectionError.FORBIDDEN_NOT_MEMBER);
		}

		User editor = userFinder.findById(user.getId());
		collectionAuthorizer.verifyMember(collection, editor); // TODO: 추후 EDITOR 이상 권한으로 변경

		collection.update(request.title());

		return CollectionResponse.from(collection);
	}

	@Transactional
	public void deleteCollection(Long collectionId, UserInfo user) {
		Collection collection = getCollection(collectionId);

		if (user.isGuest()) {
			throw new BusinessException(CollectionError.FORBIDDEN_NOT_MEMBER);
		}

		User owner = userFinder.findById(user.getId());
		collectionAuthorizer.verifyOwner(collection, owner); // 소유자만 삭제 가능

		collectionRepository.delete(collection);
	}

	private Collection getCollection(Long collectionId) {
		return collectionRepository.findById(collectionId)
			.orElseThrow(() -> new BusinessException(CollectionError.COLLECTION_NOT_FOUND));
	}

	@Transactional
	public ShareLinkResponse createInvitation(Long collectionId, UserInfo user) {
		if (user.isGuest()) {
			throw new BusinessException(CollectionError.FORBIDDEN_NOT_MEMBER);
		}
		Collection collection = getCollection(collectionId);
		User hostUser = userFinder.findById(user.getId());

		collectionAuthorizer.verifyMember(collection, hostUser);

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
		addCollectionMember(collection, inviteeUser);

		shareLink.increaseUseCount();

		return collection.getId();
	}

	private void addCollectionMember(Collection collection, User user) {
		collectionAuthorizer.checkIfMemberExists(collection, user);

		CollectionMember newMember = CollectionMember.create(collection, user, CollectionRole.MEMBER);
		collectionMemberRepository.save(newMember);

		collection.increaseMemberCount();
	}
}
