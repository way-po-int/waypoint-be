package waypoint.mvp.collection.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeCollectionOwnerRequest(
	@NotBlank(message = "owner로 변경하기 위해서는 memberId가 필요합니다.")
	String collectionMemberId) {

}
