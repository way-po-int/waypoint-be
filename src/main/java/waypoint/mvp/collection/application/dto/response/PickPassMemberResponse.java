package waypoint.mvp.collection.application.dto.response;

import waypoint.mvp.collection.domain.CollectionMember;

public record PickPassMemberResponse(
	String collectionMemberId,
	String nickname,
	String picture
) {
	public static PickPassMemberResponse from(CollectionMember member) {
		return new PickPassMemberResponse(
			member.getId().toString(),
			member.getNickname(),
			member.getPicture()
		);
	}
}
