package waypoint.mvp.notification.application.event;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.notification.application.NotificationService;
import waypoint.mvp.notification.domain.event.NotificationEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

	private final NotificationService notificationService;

	@Async("notificationTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleNotificationEvent(NotificationEvent event) {
		try {
			List<Long> receiveUserIds;
			
			if (event.isSingleUser()) {
				receiveUserIds = List.of(event.targetUserId());
			} else if (event.isMultipleUsers()) {
				receiveUserIds = event.targetUserIds();
			} else {
				return;
			}

			notificationService.createNotification(
				event.resourceId(),
				event.type(),
				event.message(),
				event.linkUrl(),
				event.sendUserId(),
				receiveUserIds
			);
		} catch (Exception e) {
			log.error("알림 생성 실패: type={}, error={}", event.type(), e.getMessage(), e);
		}
	}
}

