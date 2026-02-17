package waypoint.mvp.plan.domain;

import static waypoint.mvp.plan.domain.BlockStatus.*;

import java.time.LocalTime;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
import waypoint.mvp.global.util.TimeUtils;
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
	@OnDelete(action = OnDeleteAction.CASCADE)
	private PlanDay planDay;

	@Column(nullable = false)
	private LocalTime startTime;

	@Column(nullable = false)
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TimeBlockType type;

	@Builder(access = AccessLevel.PRIVATE)
	private TimeBlock(PlanDay planDay, LocalTime startTime, LocalTime endTime, TimeBlockType type) {
		validateTimeRange(startTime, endTime); // 생성 시점 방어 로직
		this.planDay = planDay;
		this.startTime = startTime;
		this.endTime = endTime;
		this.type = type;
	}

	public static TimeBlock create(PlanDay planDay, LocalTime startTime, LocalTime endTime, TimeBlockType type) {
		return builder()
			.planDay(planDay)
			.startTime(startTime)
			.endTime(endTime)
			.type(type)
			.build();
	}

	public static BlockStatus determine(TimeBlockType type, Block selectedBlock, List<Block> blocks) {
		if (type == TimeBlockType.FREE)
			return NOTHING;
		if (selectedBlock != null)
			return blocks.size() >= 2 ? BlockStatus.FIXED : BlockStatus.DIRECT;
		return PENDING;
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
		if (!TimeUtils.isValidRange(start, end)) {
			throw new BusinessException(TimeBlockError.INVALID_TIME_RANGE);
		}
	}

}
