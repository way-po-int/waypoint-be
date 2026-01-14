package waypoint.mvp.collection.application.dto;

import lombok.Builder;
import waypoint.mvp.collection.domain.Collection;

@Builder
public record CollectionDto(
    Long id,
    String title
) {
    public static CollectionDto from(Collection collection) {
        return CollectionDto.builder()
            .id(collection.getId())
            .title(collection.getTitle())
            .build();
    }
}
