package waypoint.mvp.auth.domain;

import org.springframework.security.core.Authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
	USER,
	PRE_TERMS;

	public String getAuthority() {
		return "ROLE_" + this.name();
	}

	public static boolean hasRole(Authentication auth, Role role) {
		if (auth == null) {
			return false;
		}
		return auth.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals(role.getAuthority()));
	}
}
