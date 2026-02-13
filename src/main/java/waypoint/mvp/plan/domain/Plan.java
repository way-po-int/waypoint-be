package waypoint.mvp.plan.domain;

import java.time.LocalDate;

import org.hibernate.annotations.SQLRestriction;

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
import waypoint.mvp.global.common.LogicalDeleteEntity;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.global.util.DateUtils;
import waypoint.mvp.plan.error.PlanError;

@Entity
@Table(name = "plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Plan extends LogicalDeleteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false)
	private int memberCount = 0;

	@Builder(access = AccessLevel.PRIVATE)
	private Plan(String title, LocalDate startDate, LocalDate endDate, int memberCount) {
		validateDateRange(startDate, endDate);
		this.title = title;
		this.startDate = startDate;
		this.endDate = endDate;
		this.memberCount = memberCount;
	}

	public static Plan create(String title, LocalDate startDate, LocalDate endDate) {
		return builder()
			.title(title)
			.startDate(startDate)
			.endDate(endDate)
			.memberCount(1)
			.build();
	}

	public void increaseMemberCount() {
		this.memberCount++;
	}

	public void decreaseMemberCount() {
		this.memberCount--;
	}

	private static void validateDateRange(LocalDate startDate, LocalDate endDate) {
		if (!DateUtils.isNotBefore(startDate, endDate)) {
			throw new BusinessException(PlanError.INVALID_DATE_RANGE);
		}
	}

	public void update(String title, LocalDate startDate, LocalDate endDate) {
		validateDateRange(startDate, endDate);
		this.title = title;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public void delete() {
		super.softDelete();
	}
}
