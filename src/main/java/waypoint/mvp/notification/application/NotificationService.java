package waypoint.mvp.notification.application;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.notification.application.dto.response.NotificationResponse;
import waypoint.mvp.notification.infrastructure.NotificationReceiveUserRepository;
import waypoint.mvp.notification.infrastructure.NotificationRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final NotificationReceiveUserRepository receiveUserRepository;

	public SliceResponse<NotificationResponse> findNotifications(UserPrincipal user, Pageable pageable) {
		Slice<NotificationResponse> notifications = receiveUserRepository
			.findByReceiveUserId(user.id(), pageable)
			.map(NotificationResponse::from);

		return SliceResponse.from(notifications);
	}

}
