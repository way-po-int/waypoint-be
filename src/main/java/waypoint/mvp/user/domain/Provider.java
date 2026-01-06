package waypoint.mvp.user.domain;

public enum Provider {
	GOOGLE, KAKAO, NAVER;

	public static Provider from(String provider) {
		return valueOf(provider.toUpperCase());
	}
}
