package waypoint.mvp.collection.presentation.dto.response;

import waypoint.mvp.collection.application.dto.CollectionDto;

public record CollectionResponse(
    Long id,
    String title
) {
    public static CollectionResponse from(CollectionDto dto) {
        return new CollectionResponse(dto.id(), dto.title());
    }
}
