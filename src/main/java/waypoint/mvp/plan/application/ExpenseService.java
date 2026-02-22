package waypoint.mvp.plan.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.Budget;
import waypoint.mvp.plan.domain.Expense;
import waypoint.mvp.plan.infrastructure.persistence.ExpenseRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpenseService {

	private final BudgetQueryService budgetQueryService;
	private final ExpenseRepository expenseRepository;

	@Transactional
	public void createBlockExpense(Long planId, Block block) {
		Budget budget = budgetQueryService.getBudget(planId);
		Expense expense = Expense.createBlock(budget, block);
		expenseRepository.save(expense);
	}

	@Transactional
	public void createBlockExpenses(Long planId, List<Block> blocks) {
		Budget budget = budgetQueryService.getBudget(planId);
		List<Expense> expenses = blocks.stream()
			.map(block -> Expense.createBlock(budget, block))
			.toList();
		expenseRepository.saveAll(expenses);
	}
}
