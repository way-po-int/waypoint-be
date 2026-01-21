package waypoint.mvp.auth.security.principal;

import java.util.Optional;

import waypoint.mvp.sharelink.domain.ShareLink;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;

/**
 * <h3>Guest 방문자 정보</h3>
 * <p>공유 링크({@link ShareLink})를 통해 한시적으로 권한을 부여받은 비로그인 사용자입니다.</p>
 * * <strong>비즈니스 제약:</strong>
 * <ul>
 * <li>{@code isGuest()}는 항상 {@code true}를 반환합니다.</li>
 * <li>영속 계정이 없으므로 {@code getId()}는 {@code null}을 반환합니다.</li>
 * <li>특정 리소스({@code targetId})에 대해서만 접근 권한을 가집니다.</li>
 * </ul>
 * * @param shareLinkCode 사용된 공유 링크 코드
 * @param targetType 허용된 리소스 타입
 * @param targetId 허용된 리소스 식별자
 */
public record GuestPrincipal(String shareLinkCode, ShareLinkType targetType, Long targetId) implements WayPointUser {

	public static GuestPrincipal from(ShareLink shareLink) {
		return new GuestPrincipal(shareLink.getCode(),
			shareLink.getTargetType(),
			shareLink.getTargetId());
	}

	@Override
	public Long getId() {
		return null; // Guest는 영속적인 ID가 없음
	}

	@Override
	public boolean isGuest() {
		return true;
	}

	@Override
	public Optional<Long> getTargetIdFor(ShareLink.ShareLinkType type) {
		if (this.targetType == type) {
			return Optional.of(targetId);
		}
		return Optional.empty();
	}
}
