package waypoint.mvp.notification.infrastructure;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.notification.domain.NotificationReceiveUser;

public interface NotificationReceiveUserRepository extends JpaRepository<NotificationReceiveUser, Long> {

	@Query("SELECT nru FROM NotificationReceiveUser nru " +
		"JOIN FETCH nru.notification n " +
		"WHERE nru.receiveUserId = :userId " +
		"ORDER BY n.createdAt DESC")
	Slice<NotificationReceiveUser> findByReceiveUserId(
		@Param("userId") Long userId,
		Pageable pageable
	);

}
