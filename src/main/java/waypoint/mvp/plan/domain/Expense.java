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
import waypoint.mvp.global.common.ExternalIdEntity;

@Entity
@Table(name = "expenses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expense extends ExternalIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Budget budget;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ExpenseType type;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(unique = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Block block;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	@OnDelete(action = OnDeleteAction.CASCADE)
	private TimeBlock timeBlock;

	@Column
	private Long rank;

	@Builder(access = AccessLevel.PRIVATE)
	private Expense(Budget budget, ExpenseType type, Block block, TimeBlock timeBlock, Long rank) {
		this.budget = budget;
		this.block = block;
		this.timeBlock = timeBlock;
		this.type = type;
		this.rank = rank;
	}

	public static Expense createAdditional(Budget budget, TimeBlock timeBlock, Long rank) {
		return builder()
			.budget(budget)
			.type(ExpenseType.ADDITIONAL)
			.timeBlock(timeBlock)
			.rank(rank)
			.build();
	}

	public static Expense createBlock(Budget budget, Block block) {
		return builder()
			.budget(budget)
			.type(ExpenseType.BLOCK)
			.block(block)
			.build();
	}

	public void updateRank(Long rank) {
		this.rank = rank;
	}
}
