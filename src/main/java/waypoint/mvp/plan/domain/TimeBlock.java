package waypoint.mvp.plan.domain;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.ExternalIdEntity;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.domain.error.TimeBlockError;

@Entity
@Table(name = "time_blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeBlock extends ExternalIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private PlanDay planDay;

	@Column(nullable = false)
	private LocalTime startTime;

	@Column(nullable = false)
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TimeBlockType type;

	@Builder
	public TimeBlock(PlanDay planDay, LocalTime startTime, LocalTime endTime, TimeBlockType type) {
		validateTimeRange(startTime, endTime); // 생성 시점 방어 로직
		this.planDay = planDay;
		this.startTime = startTime;
		this.endTime = endTime;
		this.type = type;
	}

	public void updatePlanDay(PlanDay planDay) {
		this.planDay = planDay;
	}

	public void updateTime(LocalTime startTime, LocalTime endTime) {
		validateTimeRange(startTime, endTime);
		this.startTime = startTime;
		this.endTime = endTime;
	}

	private void validateTimeRange(LocalTime start, LocalTime end) {
		// TODO timeUtil 사용으로 변경
		if (start.isAfter(end) || start.equals(end)) {
			throw new BusinessException(TimeBlockError.INVALID_TIME_RANGE);
		}
	}

}
