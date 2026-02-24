package waypoint.mvp.plan.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.plan.application.dto.request.ExpenseCreateRequest;
import waypoint.mvp.plan.application.dto.response.ExpenseGroupResponse;
import waypoint.mvp.plan.application.dto.response.ExpenseResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.Budget;
import waypoint.mvp.plan.domain.Expense;
import waypoint.mvp.plan.domain.ExpenseItem;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.infrastructure.persistence.ExpenseItemRepository;
import waypoint.mvp.plan.infrastructure.persistence.ExpenseRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpenseService {

	private final BudgetQueryService budgetQueryService;
	private final ExpenseQueryService expenseQueryService;
	private final ExpenseRankService expenseRankService;
	private final ResourceAuthorizer planAuthorizer;

	private final ExpenseRepository expenseRepository;
	private final ExpenseItemRepository expenseItemRepository;

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

	@Transactional
	public ExpenseGroupResponse createAdditionalExpense(
		String planExternalId,
		ExpenseCreateRequest request,
		UserPrincipal user
	) {
		Budget budget = budgetQueryService.getBudget(planExternalId);
		planAuthorizer.verifyMember(user, budget.getPlan().getId());

		Expense prevExpense = expenseQueryService.getExpenseWithLock(request.prevExpenseId());
		Long rank = expenseRankService.generateRank(prevExpense);

		Expense expense = Expense.createAdditional(budget, prevExpense.getTimeBlock(), prevExpense.getPlanDay(), rank);
		expenseRepository.save(expense);

		List<ExpenseItem> items = request.items().stream()
			.map(item -> ExpenseItem.create(expense, item.name(), item.cost()))
			.toList();
		expenseItemRepository.saveAll(items);

		return ExpenseGroupResponse.ofAdditional(ExpenseResponse.of(expense, items));
	}

	@Transactional
	public void relocateExpenses(Long timeBlockId, Long planDayId, TimeBlock prevTimeBlock) {
		expenseRankService.relocate(timeBlockId, planDayId, prevTimeBlock);
	}
}
