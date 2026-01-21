package waypoint.mvp.auth.security.principal;

import java.util.Optional;

import waypoint.mvp.sharelink.domain.ShareLink;

/**
 * <h3>인증 주체 공통 인터페이스</h3>
 * <p>Spring Security의 Principal을 애플리케이션 도메인 모델로 추상화합니다.</p>
 * * <strong>주요 역할:</strong>
 * <ul>
 * <li>정규 사용자({@link UserInfo})와 Guest({@link GuestPrincipal})의 다형성 처리</li>
 * <li>서비스 레이어의 인증 주체 판별 로직(instanceof) 제거</li>
 * </ul>
 * @see UserInfo
 * @see GuestPrincipal
 */
public interface WayPointUser {

	Long getId();

	boolean isGuest();

	default Optional<Long> getTargetIdFor(ShareLink.ShareLinkType type) {
		return Optional.empty();
	}
}
