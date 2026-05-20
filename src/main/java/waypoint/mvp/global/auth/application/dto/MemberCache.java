package waypoint.mvp.global.auth.application.dto;

import java.util.Set;

/**
 * 멤버십 캐시 데이터를 담는 불변 객체
 * Collection 또는 Plan의 멤버 정보를 캐시하여 DB 조회를 최소화
 */
public record MemberCache(
	Set<Long> memberUserIds,
	Long ownerId
) {
	public boolean isMember(Long userId) {
		return memberUserIds != null && memberUserIds.contains(userId);
	}

	public boolean isOwner(Long userId) {
		return userId != null && userId.equals(ownerId);
	}

	public boolean isEmpty() {
		return memberUserIds == null || memberUserIds.isEmpty();
	}
}
