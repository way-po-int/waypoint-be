package waypoint.mvp.plan.domain;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expenses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Budget budget;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private PlanDay planDay;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Block block;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ExpenseType type;

	@Column(nullable = false)
	private Long rank;

	@Builder(access = AccessLevel.PRIVATE)
	private Expense(Budget budget, PlanDay planDay, Block block, ExpenseType type, Long rank) {
		this.budget = budget;
		this.planDay = planDay;
		this.block = block;
		this.type = type;
		this.rank = rank;
	}

	public static Expense createAdditional(Budget budget, PlanDay planDay, Long rank) {
		return builder()
			.budget(budget)
			.planDay(planDay)
			.type(ExpenseType.ADDITIONAL)
			.rank(rank)
			.build();
	}

	public static Expense createBlock(Budget budget, PlanDay planDay, Block block, Long rank) {
		return builder()
			.budget(budget)
			.planDay(planDay)
			.block(block)
			.type(ExpenseType.BLOCK)
			.rank(rank)
			.build();
	}

	public void updateDay(PlanDay planDay, Long rank) {
		this.planDay = planDay;
		updateRank(rank);
	}

	public void updateRank(Long rank) {
		this.rank = rank;
	}
}
