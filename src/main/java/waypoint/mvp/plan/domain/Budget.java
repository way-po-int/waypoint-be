package waypoint.mvp.plan.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.ExternalIdEntity;

@Entity
@Table(name = "budgets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Budget extends ExternalIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BudgetType type;

	@Column(nullable = false)
	private Long totalBudget;

	@Column
	private Integer travelerCount;

	@Builder(access = AccessLevel.PRIVATE)
	private Budget(BudgetType type, Long totalBudget, Integer travelerCount) {
		this.type = type;
		this.totalBudget = totalBudget;
		this.travelerCount = travelerCount;
	}

	public static Budget create() {
		return builder()
			.type(BudgetType.BUDGET)
			.totalBudget(0L)
			.build();
	}

	public void updateBudgetType(Long totalBudget, Integer travelerCount) {
		this.type = BudgetType.BUDGET;
		this.totalBudget = totalBudget;
		this.travelerCount = travelerCount;
	}

	public void updateExpenseType(Integer travelerCount) {
		this.type = BudgetType.EXPENSE;
		this.travelerCount = travelerCount;
	}

	public Long getCostPerPerson() {
		if (travelerCount == null || travelerCount == 0) {
			return totalBudget;
		}
		return Math.round((double)totalBudget / travelerCount);
	}
}
