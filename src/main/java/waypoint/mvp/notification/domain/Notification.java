package waypoint.mvp.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.BaseTimeEntity;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long resourceId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private NotificationEventType type;

	@Column(nullable = false)
	private String message;

	@Column(length = 1024)
	private String linkUrl;

	@Column(nullable = false)
	private Long sendUserId;

	@Builder(access = AccessLevel.PRIVATE)
	private Notification(Long resourceId, NotificationEventType type, String message, String linkUrl, Long sendUserId) {
		this.resourceId = resourceId;
		this.type = type;
		this.message = message;
		this.linkUrl = linkUrl;
		this.sendUserId = sendUserId;

	}

	public static Notification create(
		Long resourceId,
		NotificationEventType type,
		String message,
		String linkUrl,
		Long sendUserId
	) {
		return builder()
			.resourceId(resourceId)
			.type(type)
			.message(message)
			.linkUrl(linkUrl)
			.sendUserId(sendUserId)
			.build();
	}
}
