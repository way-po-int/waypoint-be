package waypoint.mvp.collection.application.dto.response;

import java.util.List;

public record PickPassResponse(
	List<PickPassMemberResponse> pickedMember,
	List<PickPassMemberResponse> passedMember
) {
	public static PickPassResponse of(
		List<PickPassMemberResponse> pickedMember,
		List<PickPassMemberResponse> passedMember
	) {
		return new PickPassResponse(pickedMember, passedMember);
	}
}
