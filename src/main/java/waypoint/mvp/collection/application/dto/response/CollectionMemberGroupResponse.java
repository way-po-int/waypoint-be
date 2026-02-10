package waypoint.mvp.collection.application.dto.response;

import java.util.List;

public record CollectionMemberGroupResponse(
	CollectionMemberResponse me,
	List<CollectionMemberResponse> members,
	Boolean isAuthenticated
) {
}
