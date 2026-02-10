package waypoint.mvp.collection.application.response;

import java.util.List;

import waypoint.mvp.collection.application.dto.response.CollectionMemberResponse;

public record CollectionMemberGroupResponse(
	CollectionMemberResponse me,
	List<CollectionMemberResponse> members,
	boolean isAuthenticated
) {
}
