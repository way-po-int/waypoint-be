package waypoint.mvp.notification.application;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.collection.application.CollectionMemberService;
import waypoint.mvp.notification.domain.NotificationEventType;
import waypoint.mvp.notification.domain.event.NotificationEvent;
import waypoint.mvp.plan.application.PlanMemberService;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

	private final PlanMemberService planMemberService;
	private final CollectionMemberService collectionMemberService;
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

	public void publishPlanTeamNotification(
		Long planId,
		String planExternalId,
		Long actorUserId,
		NotificationEventType notificationEventType,
		Map<String, Object> metadata
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
				"/plans/" + planExternalId,
				actorUserId,
				memberUserIds,
				metadata
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

	public void publishCollectionTeamNotification(
		Long collectionId,
		String collectionExternalId,
		Long actorUserId,
		NotificationEventType notificationEventType,
		Map<String, Object> metadata
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
				"/collections/" + collectionExternalId,
				actorUserId,
				memberUserIds,
				metadata
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

	public void publishPersonalNotification(
		Long resourceId,
		NotificationEventType type,
		String linkUrl,
		Long sendUserId,
		Long targetUserId,
		Map<String, Object> metadata
	) {
		eventPublisher.publishEvent(
			NotificationEvent.forSingleUser(
				resourceId,
				type,
				linkUrl,
				sendUserId,
				targetUserId,
				metadata
			)
		);
	}
}
