package waypoint.mvp.plan.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.error.ExpenseError;

@Entity
@Table(name = "expense_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenseItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Expense expense;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Long cost;

	@Builder(access = AccessLevel.PRIVATE)
	private ExpenseItem(Expense expense, String name, Long cost) {
		this.expense = expense;
		this.name = name;
		this.cost = cost;
	}

	public static ExpenseItem create(Expense expense, String name, Long cost) {
		validateCost(cost);
		return builder()
			.expense(expense)
			.name(name)
			.cost(cost)
			.build();
	}

	public void update(String name, Long cost) {
		validateCost(cost);
		this.name = name;
		this.cost = cost;
	}

	private static void validateCost(Long cost) {
		if (cost == null || cost < 0) {
			throw new BusinessException(ExpenseError.INVALID_ITEM_COST);
		}
	}
}
