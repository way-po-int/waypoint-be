package waypoint.mvp.notification.application.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import waypoint.mvp.notification.domain.Notification;
import waypoint.mvp.notification.domain.NotificationEventType;
import waypoint.mvp.notification.domain.NotificationReceiveUser;

public record NotificationResponse(
	String notificationId,
	NotificationEventType.Category category,
	NotificationEventType.ActionType actionType,
	String message,
	String linkUrl,
	boolean isRead,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
	Instant readAt,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
	Instant createdAt
) {
	public static NotificationResponse from(NotificationReceiveUser receiveUser) {
		Notification notification = receiveUser.getNotification();
		NotificationEventType type = notification.getType();
		return new NotificationResponse(
			String.valueOf(notification.getId()),
			type.getCategory(),
			type.getActionType(),
			notification.getMessage(),
			notification.getLinkUrl(),
			receiveUser.isRead(),
			receiveUser.getReadAt(),
			notification.getCreatedAt()
		);
	}
}
