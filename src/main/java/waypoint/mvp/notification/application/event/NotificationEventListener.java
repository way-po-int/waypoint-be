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
import waypoint.mvp.user.application.UserFinder;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

	private final NotificationService notificationService;
	private final UserFinder userFinder;

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

			String message = buildMessage(event);

			notificationService.createNotification(
				event.resourceId(),
				event.type(),
				message,
				event.linkUrl(),
				event.sendUserId(),
				receiveUserIds
			);
		} catch (Exception e) {
			log.error("알림 생성 실패: type={}, error={}", event.type(), e.getMessage(), e);
		}
	}

	private String buildMessage(NotificationEvent event) {
		if (event.message() != null) {
			return event.message();
		}

		if (event.metadata() == null) {
			throw new IllegalArgumentException("메시지와 메타데이터가 모두 null입니다");
		}

		String actorNickname = userFinder.findById(event.sendUserId()).getNickname();
		String resourceTitle = (String) event.metadata().get("resourceTitle");
		String targetName = (String) event.metadata().get("targetName");

		return event.type().buildMessage(actorNickname, resourceTitle, targetName);
	}
}

