package waypoint.mvp.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "user_withdrawal_reasons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserWithdrawalReason extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false, length = 500)
	private String reason;

	@Builder(access = AccessLevel.PRIVATE)
	private UserWithdrawalReason(Long userId, String reason) {
		this.userId = userId;
		this.reason = reason;
	}

	public static UserWithdrawalReason create(Long userId, String reason) {
		return builder()
			.userId(userId)
			.reason(reason)
			.build();
	}
}
