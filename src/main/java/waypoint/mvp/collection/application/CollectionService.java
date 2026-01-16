package waypoint.mvp.collection.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserInfo;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.event.CollectionCreatedEvent;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.collection.application.dto.request.CollectionCreateRequest;
import waypoint.mvp.collection.application.dto.request.CollectionUpdateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionResponse;
import waypoint.mvp.global.error.exception.BusinessException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionService {

	private final CollectionRepository collectionRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public Long createCollection(CollectionCreateRequest request, UserInfo user) {
		Collection collection = Collection.create(request.title());
		collectionRepository.save(collection);

		eventPublisher.publishEvent(CollectionCreatedEvent.of(collection.getId(), user));

		return collection.getId();
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
	public void updateCollection(Long collectionId, CollectionUpdateRequest request) {
		Collection collection = getCollection(collectionId);
		collection.update(request.title());
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
