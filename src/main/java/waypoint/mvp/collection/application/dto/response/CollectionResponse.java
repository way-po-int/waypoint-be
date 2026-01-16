package waypoint.mvp.collection.application.dto.response;

import waypoint.mvp.collection.domain.Collection;

public record CollectionResponse(
    Long id,
    String title
) {
        public static CollectionResponse from(Collection collection) {
        return new CollectionResponse(collection.getId(), collection.getTitle());
    }
}
