package waypoint.mvp.collection.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import waypoint.mvp.collection.presentation.dto.CollectionCreateRequest;
import waypoint.mvp.collection.application.dto.CollectionDto;
import waypoint.mvp.collection.presentation.dto.CollectionUpdateRequest;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;

    @Transactional
    public Long createCollection(CollectionCreateRequest request) {
        Collection collection = Collection.create(request.title());
        return collectionRepository.save(collection).getId();
    }

        public Page<CollectionDto> findCollections(Pageable pageable) {
        return collectionRepository.findAll(pageable)
            .map(CollectionDto::from);
    }

        public CollectionDto findCollectionById(Long collectionId) {
        return collectionRepository.findById(collectionId)
            .map(CollectionDto::from)
            .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
    }

    @Transactional
    public void updateCollection(Long collectionId, CollectionUpdateRequest request) {
        Collection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        collection.update(request.title());
    }

    @Transactional
    public void deleteCollection(Long collectionId) {
        collectionRepository.deleteById(collectionId);
    }
}
