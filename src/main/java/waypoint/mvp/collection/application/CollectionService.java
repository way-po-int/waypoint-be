package waypoint.mvp.collection.application;

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
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.user.domain.User;
import waypoint.mvp.user.error.UserError;
import waypoint.mvp.user.infrastructure.persistence.UserRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionService {

	private final CollectionRepository collectionRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final UserRepository userRepository;
	private final CollectionMemberRepository collectionMemberRepository;

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
	public void addCollectionMember(Long collectionId, Long userId) {
		Collection collection = getCollection(collectionId);
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		// TODO: 1. 이미 컬렉션에 속한 멤버인지 확인 & count 로직 추가

		// TODO: 2. 컬렉션 최대 멤버 수 제한 로직 추가 (필요 시)

		CollectionMember newMember = CollectionMember.create(collection, user, CollectionRole.MEMBER);
		collectionMemberRepository.save(newMember);

		collection.increaseMemberCount();
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

}
