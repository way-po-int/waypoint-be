package waypoint.mvp.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.user.domain.UserWithdrawalReason;

public interface UserWithdrawalReasonRepository extends JpaRepository<UserWithdrawalReason, Long> {
}
