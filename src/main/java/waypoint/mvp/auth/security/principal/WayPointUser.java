package waypoint.mvp.auth.security.principal;

/**
 * <h3>인증 주체 공통 인터페이스</h3>
 * <p>Spring Security의 Principal을 애플리케이션 도메인 모델로 추상화합니다.</p>
 * <strong>주요 역할:</strong>
 * <ul>
 * <li>정규 사용자({@link UserPrincipal})와 Guest({@link GuestPrincipal})의 다형성 처리</li>
 * </ul>
 * @see UserPrincipal
 * @see GuestPrincipal
 */
public interface WayPointUser {

	Long getId();

	boolean isGuest();
}
