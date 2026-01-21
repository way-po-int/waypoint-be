package waypoint.mvp.global.auth.annotations;

public enum AuthLevel {
	AUTHENTICATED, // 로그인한 모든 사용자(Guest 제외)
	MEMBER,
	OWNER,
	GUEST_OR_MEMBER
}
