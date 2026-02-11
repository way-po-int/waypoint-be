package waypoint.mvp.collection.application.dto.response;

import java.util.List;

public record CollectionMemberGroupResponse(
	Boolean isAuthenticated,
	CollectionMemberResponse me,
	List<CollectionMemberResponse> members
) {
}
