package waypoint.mvp.notification.application;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.collection.application.CollectionMemberService;
import waypoint.mvp.notification.domain.NotificationEventType;
import waypoint.mvp.notification.domain.event.NotificationEvent;
import waypoint.mvp.plan.application.PlanMemberService;
import waypoint.mvp.user.application.UserFinder;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

	private final PlanMemberService planMemberService;
	private final CollectionMemberService collectionMemberService;
	private final UserFinder userFinder;
	private final ApplicationEventPublisher eventPublisher;

	public void publishPlanTeamNotification(
		Long planId,
		String planExternalId,
		Long actorUserId,
		NotificationEventType notificationEventType,
		String message
	) {
		List<Long> memberUserIds = planMemberService.findMembers(planId)
			.stream()
			.map(m -> m.getUser().getId())
			.filter(id -> !id.equals(actorUserId))
			.toList();

		if (memberUserIds.isEmpty()) {
			return;
		}

		eventPublisher.publishEvent(
			NotificationEvent.forMultipleUsers(
				planId,
				notificationEventType,
				message,
				"/plans/" + planExternalId,
				actorUserId,
				memberUserIds
			)
		);
	}

	public void publishCollectionTeamNotification(
		Long collectionId,
		String collectionExternalId,
		Long actorUserId,
		NotificationEventType notificationEventType,
		String message
	) {
		List<Long> memberUserIds = collectionMemberService.findMembers(collectionId)
			.stream()
			.map(m -> m.getUser().getId())
			.filter(id -> !id.equals(actorUserId))
			.toList();

		if (memberUserIds.isEmpty()) {
			return;
		}

		eventPublisher.publishEvent(
			NotificationEvent.forMultipleUsers(
				collectionId,
				notificationEventType,
				message,
				"/collections/" + collectionExternalId,
				actorUserId,
				memberUserIds
			)
		);
	}

	public void publishPersonalNotification(
		Long resourceId,
		NotificationEventType type,
		String message,
		String linkUrl,
		Long sendUserId,
		Long targetUserId
	) {
		eventPublisher.publishEvent(
			NotificationEvent.forSingleUser(
				resourceId,
				type,
				message,
				linkUrl,
				sendUserId,
				targetUserId
			)
		);
	}

	public String getUserNickname(Long userId) {
		return userFinder.findById(userId).getNickname();
	}
}
