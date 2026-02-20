package waypoint.mvp.notification.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.BaseTimeEntity;

@Entity
@Table(
	name = "notification_receive_user",
	indexes = {
		@Index(name = "idx_receive_user_id", columnList = "receive_user_id"),
		@Index(name = "idx_notification_id", columnList = "notification_id"),
		@Index(name = "idx_read_at", columnList = "read_at")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationReceiveUser extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Notification notification;

	@Column(nullable = false)
	private Long receiveUserId;

	private Instant readAt;

	@Builder
	private NotificationReceiveUser(Notification notification, Long receiveUserId, Instant readAt) {
		this.notification = notification;
		this.receiveUserId = receiveUserId;
		this.readAt = readAt;
	}

	public static NotificationReceiveUser create(Notification notification, Long receiveUserId) {
		return builder()
			.notification(notification)
			.receiveUserId(receiveUserId)
			.build();

	}

	public void markAsRead() {
		this.readAt = Instant.now();
	}

	public boolean isRead() {
		return readAt != null;
	}

}
