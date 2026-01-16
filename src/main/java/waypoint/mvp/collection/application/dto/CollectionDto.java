package waypoint.mvp.collection.application.dto;

import waypoint.mvp.collection.domain.Collection;

public record CollectionDto(
    Long id,
    String title
) {
    public static CollectionDto from(Collection collection) {
        return new CollectionDto(collection.getId(), collection.getTitle());
    }
}
