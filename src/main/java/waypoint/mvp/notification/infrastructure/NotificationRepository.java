package waypoint.mvp.notification.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
