package waypoint.mvp.collection.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.application.dto.request.CollectionCreateRequest;
import waypoint.mvp.collection.application.dto.request.CollectionUpdateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionMemberGroupResponse;
import waypoint.mvp.collection.application.dto.response.CollectionMemberResponse;
import waypoint.mvp.collection.application.dto.response.CollectionResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionRole;
import waypoint.mvp.collection.domain.CollectionSortType;
import waypoint.mvp.collection.domain.event.CollectionCreatedEvent;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.common.SliceResponse;
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
	private final ResourceAuthorizer collectionAuthorizer;
	private final CollectionPlaceRepository collectionPlaceRepository;

	@Value("${waypoint.invitation.expiration-hours}")
	private long invitationExpirationHours;
	@Value("${waypoint.sharelink.base-url}")
	private String shareLinkBaseUrl;

	@Transactional
	public CollectionResponse createCollection(CollectionCreateRequest request, UserPrincipal user) {
		Collection collection = Collection.create(request.title());
		Collection savedCollection = collectionRepository.save(collection);

		eventPublisher.publishEvent(CollectionCreatedEvent.of(savedCollection.getId(), user)); // 이벤트는 실제 유저만 발생시키므로 캐스팅

		return CollectionResponse.from(savedCollection, 0);
	}

	public Collection getCollection(Long collectionId) {
		return collectionRepository.findById(collectionId)
			.orElseThrow(() -> new BusinessException(CollectionError.COLLECTION_NOT_FOUND));
	}

	public Collection getCollection(String externalId) {
		return collectionRepository.findByExternalId(externalId)
			.orElseThrow(() -> new BusinessException(CollectionError.COLLECTION_NOT_FOUND));
	}

	public List<Collection> getCollections(List<String> externalIds) {
		return collectionRepository.findAllByExternalIdIn(externalIds);
	}

	public SliceResponse<CollectionResponse> findCollections(
		UserPrincipal user,
		CollectionSortType sortType,
		Pageable pageable
	) {
		Slice<Collection> collectionsSlice = switch (sortType) {
			case LATEST -> collectionRepository.findAllByUserIdOrderByAddedLatest(user.id(), pageable);
			case OLDEST -> collectionRepository.findAllByUserIdOrderByAddedOldest(user.id(), pageable);
		};

		List<Long> collectionIds = collectionsSlice.getContent().stream()
			.map(Collection::getId)
			.toList();

		if (collectionIds.isEmpty()) {
			return SliceResponse.from(collectionsSlice.map(c -> CollectionResponse.from(c, 0)));
		}

		Map<Long, Integer> placeCounts = collectionPlaceRepository.countPlacesByCollectionIds(collectionIds).stream()
			.collect(java.util.stream.Collectors.toMap(
				row -> ((Number)row[0]).longValue(),
				row -> ((Number)row[1]).intValue()
			));

		Slice<CollectionResponse> responses = collectionsSlice.map(collection -> {
			int placeCount = placeCounts.getOrDefault(collection.getId(), 0);
			return CollectionResponse.from(collection, placeCount);
		});

		return SliceResponse.from(responses);
	}

	/** Guest or Member 사용 가능한 메서드 */
	public CollectionResponse findCollectionById(Long collectionId, AuthPrincipal user) {
		collectionAuthorizer.verifyAccess(user, collectionId);
		Collection collection = getCollection(collectionId);

		return toCollectionResponse(collection);
	}

	public CollectionResponse findCollectionByExternalId(String externalId, AuthPrincipal user) {
		Collection collection = getCollection(externalId);
		collectionAuthorizer.verifyAccess(user, collection.getId());

		return toCollectionResponse(collection);
	}

	@Transactional
	public CollectionResponse updateCollection(String externalId, CollectionUpdateRequest request, UserPrincipal user) {
		Collection collection = getCollection(externalId);
		collectionAuthorizer.verifyMember(user, collection.getId());
		collection.update(request.title());

		return toCollectionResponse(collection);
	}

	@Transactional
	public void changeOwner(String externalId, String memberExternalId, UserPrincipal user) {
		Collection collection = getCollection(externalId);
		Long collectionId = collection.getId();
		collectionAuthorizer.verifyOwner(user, collectionId);

		CollectionMember currentOwner = collectionMemberService.findMemberByUserId(collectionId, user.id());
		CollectionMember newOwner = collectionMemberService.findMember(collectionId, memberExternalId);

		if (collectionMemberService.isSameMember(currentOwner, newOwner)) {
			throw new BusinessException(CollectionError.CANNOT_DELEGATE_OWNERSHIP_TO_SELF, newOwner.getNickname());
		}

		currentOwner.updateRole(CollectionRole.MEMBER);
		newOwner.updateRole(CollectionRole.OWNER);
	}

	public CollectionMemberGroupResponse findCollectionMemberGroup(String externalId, AuthPrincipal user) {
		Collection collection = getCollection(externalId);
		Long collectionId = collection.getId();
		collectionAuthorizer.verifyAccess(user, collectionId);

		List<CollectionMember> members = collectionMemberService.findMembers(collectionId);
		Long currentUserId = (user instanceof UserPrincipal up) ? up.getId() : null;
		boolean isAuthenticated = currentUserId != null;

		List<CollectionMemberResponse> allResponses = new ArrayList<>(members.size());
		CollectionMemberResponse me = null;

		for (CollectionMember member : members) {
			CollectionMemberResponse m = CollectionMemberResponse.from(member);
			allResponses.add(m);

			if (me == null && member.getUser().getId().equals(currentUserId)) {
				me = m;
			}
		}

		return new CollectionMemberGroupResponse(isAuthenticated, me, allResponses);
	}

	@Transactional
	public void withdrawCollectionMember(String externalId, UserPrincipal user) {
		Collection collection = getCollection(externalId);
		collectionMemberService.withdraw(collection.getId(), user);
	}

	@Transactional
	public void expelCollectionMember(String externalId, String memberExternalId, UserPrincipal user) {
		Collection collection = getCollection(externalId);

		collectionMemberService.expel(collection.getId(), memberExternalId, user);
	}

	@Transactional
	public void deleteCollection(String externalId, UserPrincipal user) {
		Collection collection = getCollection(externalId);
		Long collectionId = collection.getId();
		collectionAuthorizer.verifyOwner(user, collectionId);

		collection.delete();
	}

	@Transactional
	public ShareLinkResponse createInvitation(String collectionId, UserPrincipal user) {
		Collection collection = getCollection(collectionId);
		collectionAuthorizer.verifyMember(user, collection.getId());

		ShareLink shareLink = ShareLink.create(ShareLink.ShareLinkType.COLLECTION, collection.getExternalId(),
			collection.getId(), user.getId(), invitationExpirationHours);

		shareLinkRepository.save(shareLink);

		String url = buildShareLinkUrl(shareLink.getCode());
		return ShareLinkResponse.from(shareLink, url);
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

	private CollectionResponse toCollectionResponse(Collection collection) {
		long placeCount = collectionPlaceRepository.countByCollectionId(collection.getId());
		return CollectionResponse.from(collection, (int)placeCount);
	}

	private String buildShareLinkUrl(String code) {
		String baseUrl = shareLinkBaseUrl.endsWith("/")
			? shareLinkBaseUrl.substring(0, shareLinkBaseUrl.length() - 1)
			: shareLinkBaseUrl;

		return baseUrl + "/" + code;
	}

}
