package waypoint.mvp.collection.application;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserInfo;
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
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;
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

	@Transactional
	public CollectionResponse createCollection(CollectionCreateRequest request, UserInfo user) {
		Collection collection = Collection.create(request.title());
		collectionRepository.save(collection);

		eventPublisher.publishEvent(CollectionCreatedEvent.of(collection.getId(), user));

		return CollectionResponse.from(collection);
	}

	public Page<CollectionResponse> findCollections(Pageable pageable) {
		return collectionRepository.findAll(pageable)
			.map(CollectionResponse::from);
	}

	public CollectionResponse findCollectionById(Long collectionId) {
		Collection collection = getCollection(collectionId);
		return CollectionResponse.from(collection);
	}

	@Transactional
	public CollectionResponse updateCollection(Long collectionId, CollectionUpdateRequest request) {
		Collection collection = getCollection(collectionId);
		collection.update(request.title());

		return CollectionResponse.from(collection);
	}

	@Transactional
	public void deleteCollection(Long collectionId) {
		Collection collection = getCollection(collectionId);
		collectionRepository.delete(collection);
	}

	private Collection getCollection(Long collectionId) {
		return collectionRepository.findById(collectionId)
			.orElseThrow(() -> new BusinessException(CollectionError.COLLECTION_NOT_FOUND));
	}

	@Transactional
	public ShareLinkResponse createInvitation(Long collectionId, Long hostUserId) {
		Collection collection = getCollection(collectionId);
		User hostUser = userFinder.findById(hostUserId);

		collectionAuthorizer.verifyOwner(collection, hostUser);

		String code = UUID.randomUUID().toString();
		LocalDateTime expiresAt = LocalDateTime.now().plusHours(8);
		ShareLink shareLink = ShareLink.create(code, ShareLinkType.COLLECTION, collectionId, hostUserId,
			expiresAt.toInstant(ZoneOffset.of("+09:00")));

		shareLinkRepository.save(shareLink);

		return ShareLinkResponse.from(shareLink);
	}

	@Transactional
	public void acceptInvitation(String code, Long userId) {
		ShareLink shareLink = shareLinkRepository.findByCode(code)
			.orElseThrow(() -> new BusinessException(CollectionError.INVALID_INVITATION_TOKEN));

		if (shareLink.isExpired() || shareLink.getTargetType() != ShareLinkType.COLLECTION) {
			throw new BusinessException(CollectionError.INVALID_INVITATION_TOKEN);
		}

		User user = userFinder.findById(userId);
		Collection collection = getCollection(shareLink.getTargetId());
		addCollectionMember(collection, user);

		shareLink.increaseUseCount();
	}

	private void addCollectionMember(Collection collection, User user) {
		collectionAuthorizer.checkIfMemberExists(collection, user);

		CollectionMember newMember = CollectionMember.create(collection, user, CollectionRole.MEMBER);
		collectionMemberRepository.save(newMember);

		collection.increaseMemberCount();
	}
}
