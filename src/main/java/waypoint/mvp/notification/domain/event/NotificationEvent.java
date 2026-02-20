package waypoint.mvp.notification.domain.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import waypoint.mvp.notification.domain.NotificationEventType;

public record NotificationEvent(
	Long resourceId,
	NotificationEventType type,
	String message,
	String linkUrl,
	Long sendUserId,
	Long targetUserId,
	List<Long> targetUserIds,
	Map<String, Object> metadata
) {
	public static NotificationEvent forSingleUser(
		Long resourceId,
		NotificationEventType type,
		String message,
		String linkUrl,
		Long sendUserId,
		Long targetUserId
	) {
		return new NotificationEvent(
			resourceId, type, message, linkUrl, sendUserId,
			targetUserId, null, null
		);
	}

	public static NotificationEvent forSingleUser(
		Long resourceId,
		NotificationEventType type,
		String linkUrl,
		Long sendUserId,
		Long targetUserId,
		Map<String, Object> metadata
	) {
		return new NotificationEvent(
			resourceId, type, null, linkUrl, sendUserId,
			targetUserId, null, metadata
		);
	}

	public static NotificationEvent forMultipleUsers(
		Long resourceId,
		NotificationEventType type,
		String message,
		String linkUrl,
		Long sendUserId,
		List<Long> targetUserIds
	) {
		return new NotificationEvent(
			resourceId, type, message, linkUrl, sendUserId,
			null, targetUserIds, null
		);
	}

	public static NotificationEvent forMultipleUsers(
		Long resourceId,
		NotificationEventType type,
		String linkUrl,
		Long sendUserId,
		List<Long> targetUserIds,
		Map<String, Object> metadata
	) {
		return new NotificationEvent(
			resourceId, type, null, linkUrl, sendUserId,
			null, targetUserIds, metadata
		);
	}

	public static Map<String, Object> createMetadata(String resourceTitle, String targetName) {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("resourceTitle", resourceTitle);
		metadata.put("targetName", targetName);
		return metadata;
	}

	public boolean isSingleUser() {
		return targetUserId != null;
	}

	public boolean isMultipleUsers() {
		return targetUserIds != null && !targetUserIds.isEmpty();
	}
}
