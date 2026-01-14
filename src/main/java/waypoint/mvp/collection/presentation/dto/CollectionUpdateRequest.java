package waypoint.mvp.collection.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CollectionUpdateRequest(
    @NotBlank(message = "컬렉션 제목은 비워둘 수 없습니다.")
    String title
) {
}
