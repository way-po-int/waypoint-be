package waypoint.mvp.collection.application.dto.response;

import waypoint.mvp.collection.domain.CollectionMember;

public record CollectionMemberResponse(
	String collectionMemberId,
	String nickname,
	String picture,
	String role
) {
	public static CollectionMemberResponse from(CollectionMember member) {
		return new CollectionMemberResponse(member.getId().toString(), member.getNickname(),
			member.getPicture(), member.getRole().toString());
	}
}
