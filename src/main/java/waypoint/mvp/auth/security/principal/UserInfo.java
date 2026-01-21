package waypoint.mvp.auth.security.principal;

import io.jsonwebtoken.Claims;

/**
 * <h3>정규 인증 사용자 정보</h3>
 * <p>JWT 또는 OIDC를 통해 인증된 시스템의 정식 사용자를 나타냅니다.</p>
 * * <strong>비즈니스 제약:</strong>
 * <ul>
 * <li>{@code isGuest()}는 항상 {@code false}를 반환합니다.</li>
 * <li>{@code getId()}는 항상 영속화된 유효한 ID를 반환합니다.</li>
 * </ul>
 * * @param id 시스템 내부 사용자 식별 고유 ID
 */
public record UserInfo(Long id) implements WayPointUser {

	public static UserInfo from(Claims claims) {
		return new UserInfo(
			Long.valueOf(claims.getSubject())
		);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public boolean isGuest() {
		return false;
	}

	public static UserInfo from(Object principal) {
		if (principal instanceof CustomOidcUser oidcUser) {
			return new UserInfo(oidcUser.getId());
		}
		return (UserInfo)principal;
	}
}
