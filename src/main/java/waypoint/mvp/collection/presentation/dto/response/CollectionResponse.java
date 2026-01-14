package waypoint.mvp.collection.presentation.dto.response;

import lombok.Builder;
import waypoint.mvp.collection.application.dto.CollectionDto;

@Builder
public record CollectionResponse(
    Long id,
    String title
) {
    public static CollectionResponse from(CollectionDto dto) {
        return CollectionResponse.builder()
            .id(dto.id())
            .title(dto.title())
            .build();
    }
}
