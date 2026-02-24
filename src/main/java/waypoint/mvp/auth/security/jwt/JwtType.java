package waypoint.mvp.auth.security.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtType {
	ACCESS("at"),
	ONBOARDING_ACCESS("oat"),
	REFRESH("rt");

	private final String value;
}
