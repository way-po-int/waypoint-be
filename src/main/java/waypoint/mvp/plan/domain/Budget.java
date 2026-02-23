package waypoint.mvp.plan.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.ExternalIdEntity;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.error.BudgetError;

@Entity
@Table(name = "budgets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Budget extends ExternalIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(unique = true, nullable = false)
	private Plan plan;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BudgetType type;

	@Column(nullable = false)
	private Long totalBudget;

	@Column
	private Integer travelerCount;

	@Builder(access = AccessLevel.PRIVATE)
	private Budget(Plan plan, BudgetType type, Long totalBudget, Integer travelerCount) {
		this.plan = plan;
		this.type = type;
		this.totalBudget = totalBudget;
		this.travelerCount = travelerCount;
	}

	public static Budget create(Plan plan) {
		return builder()
			.plan(plan)
			.type(BudgetType.BUDGET)
			.totalBudget(0L)
			.build();
	}

	public Integer getTravelerCount() {
		return Objects.requireNonNullElse(this.travelerCount, plan.getMemberCount());
	}

	public Long getCostPerPerson(long totalSpent) {
		long baseAmount = this.type == BudgetType.BUDGET
			? this.totalBudget
			: totalSpent;

		return Math.round((double)baseAmount / getTravelerCount());
	}

	public void update(BudgetType type, Long totalBudget, Integer travelerCount) {
		this.type = type;
		updateTotalBudget(totalBudget);
		updateTravelerCount(travelerCount);
	}

	private void updateTotalBudget(Long totalBudget) {
		if (totalBudget == null) {
			return;
		}
		if (totalBudget < 0) {
			throw new BusinessException(BudgetError.INVALID_TOTAL_BUDGET);
		}
		this.totalBudget = totalBudget;
	}

	private void updateTravelerCount(Integer travelerCount) {
		if (travelerCount == null) {
			return;
		}
		if (travelerCount <= 0) {
			throw new BusinessException(BudgetError.INVALID_TRAVELER_COUNT);
		}
		this.travelerCount = travelerCount;
	}
}
