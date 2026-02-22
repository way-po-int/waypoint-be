package waypoint.mvp.plan.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.plan.domain.Expense;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.infrastructure.persistence.ExpenseRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpenseRankService {

	private static final Long RANK_INCREMENT = 65536L;

	private final ExpenseRepository expenseRepository;

	public Long generateRank(Expense prevExpense) {
		TimeBlock timeBlock = prevExpense.getTimeBlock();

		Long nextRank = expenseRepository.findNextRank(timeBlock.getId(), prevExpense.getRank());

		// 이전 지출이 마지막 지출이라면 맨 뒤에 추가
		if (nextRank == null) {
			return prevExpense.getRank() + RANK_INCREMENT;
		}

		// 중간에 추가할 수 없다면 재정렬
		if (nextRank - prevExpense.getRank() <= 1) {
			rebalance(timeBlock.getId());
			nextRank = prevExpense.getRank() + RANK_INCREMENT;
		}

		// 마지막이 아니라면 중간에 추가
		return generateBetweenRanks(prevExpense.getRank(), nextRank);
	}

	public void rebalance(Long timeBlockId) {
		List<Expense> expenses = expenseRepository.findByTimeBlockId(timeBlockId);

		long rank = RANK_INCREMENT;
		for (Expense expense : expenses) {
			expense.updateRank(rank);
			rank += RANK_INCREMENT;
		}
	}

	private Long generateBetweenRanks(Long prevRank, Long nextRank) {
		return prevRank + (nextRank - prevRank) / 2;
	}
}
