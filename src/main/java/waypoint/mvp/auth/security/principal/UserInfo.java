package waypoint.mvp.auth.security.principal;

import io.jsonwebtoken.Claims;

public record UserInfo(Long id) {

	public static UserInfo from(Claims claims) {
		return new UserInfo(
			Long.valueOf(claims.getSubject())
		);
	}

	public static UserInfo from(Object principal) {
		if (principal instanceof CustomOidcUser oidcUser) {
			return new UserInfo(oidcUser.getId());
		}
		return (UserInfo)principal;
	}
}
