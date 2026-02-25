package waypoint.mvp.plan.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.PlaceCategoryService;
import waypoint.mvp.plan.application.dto.response.ExpenseGroupResponse;
import waypoint.mvp.plan.domain.Budget;
import waypoint.mvp.plan.domain.Expense;
import waypoint.mvp.plan.domain.ExpenseItem;
import waypoint.mvp.plan.domain.PlanDay;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.error.ExpenseError;
import waypoint.mvp.plan.error.PlanError;
import waypoint.mvp.plan.infrastructure.persistence.ExpenseItemRepository;
import waypoint.mvp.plan.infrastructure.persistence.ExpenseRepository;
import waypoint.mvp.plan.infrastructure.persistence.PlanDayRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpenseQueryService {

	private final PlaceCategoryService placeCategoryService;
	private final ExpenseRepository expenseRepository;
	private final ExpenseItemRepository expenseItemRepository;
	private final PlanDayRepository planDayRepository;

	public Expense getExpense(Long budgetId, String externalId) {
		return expenseRepository.findByBudgetIdAndExternalId(budgetId, externalId)
			.orElseThrow(() -> new BusinessException(ExpenseError.EXPENSE_NOT_FOUND));
	}

	public Expense getExpenseWithLock(Long budgetId, String externalId) {
		return expenseRepository.findByExternalIdWithLock(budgetId, externalId)
			.orElseThrow(() -> new BusinessException(ExpenseError.EXPENSE_NOT_FOUND));
	}

	public List<ExpenseItem> getExpenseItems(Long expenseId) {
		return expenseItemRepository.findAllByExpenseId(expenseId);
	}

	public List<ExpenseGroupResponse> findExpenses(Budget budget, int day) {
		Long planId = budget.getPlan().getId();
		Long planDayId = planDayRepository.findByPlanIdAndDay(planId, day)
			.map(PlanDay::getId)
			.orElseThrow(() -> new BusinessException(PlanError.PLAN_DAY_NOT_FOUND));

		List<Expense> expenses = expenseRepository.findAllByBudgetIdAndPlanDayId(budget.getId(), planDayId);
		Map<Long, List<ExpenseItem>> expenseItemMap = getExpenseItemMap(expenses);

		return getExpenseByTimeBlockId(expenses).entrySet().stream()
			.flatMap(entry -> toResponses(entry.getKey(), entry.getValue(), expenseItemMap).stream())
			.toList();
	}

	private Map<Long, List<ExpenseItem>> getExpenseItemMap(List<Expense> expenses) {
		List<Long> expenseIds = expenses.stream()
			.map(Expense::getId)
			.toList();

		return expenseItemRepository.findAllByExpenseIdIn(expenseIds).stream()
			.collect(Collectors.groupingBy(item -> item.getExpense().getId()));
	}

	private Map<String, List<Expense>> getExpenseByTimeBlockId(List<Expense> expenses) {
		return expenses.stream()
			.collect(Collectors.groupingBy(
				e -> {
					TimeBlock timeBlock = e.getTimeBlock();
					return timeBlock != null ? timeBlock.getExternalId() : "";
				},
				LinkedHashMap::new,
				Collectors.toList()
			));
	}

	private List<ExpenseGroupResponse> toResponses(
		String timeBlockId,
		List<Expense> groupExpenses,
		Map<Long, List<ExpenseItem>> expenseItemMap
	) {
		// 맨 앞에 있는 추가 지출은 timeBlockId가 없음
		if (timeBlockId.isBlank()) {
			return toAdditionalResponses(groupExpenses, expenseItemMap);
		}

		Expense selectedExpense = null;
		List<Expense> candidateExpenses = new ArrayList<>();
		List<Expense> additionalExpenses = new ArrayList<>();

		for (Expense expense : groupExpenses) {
			if (expense.isAdditionalType()) {
				additionalExpenses.add(expense);
				continue;
			}
			if (expense.getBlock().isSelected()) {
				selectedExpense = expense;
			} else {
				candidateExpenses.add(expense);
			}
		}

		List<ExpenseGroupResponse> responses = new ArrayList<>();

		// 블록 지출을 결과에 추가
		responses.add(ExpenseGroupResponse.ofBlock(
			timeBlockId,
			selectedExpense,
			candidateExpenses,
			expenseItemMap,
			placeCategoryService::toCategoryResponse
		));

		// 추가 지출을 결과에 추가
		responses.addAll(toAdditionalResponses(additionalExpenses, expenseItemMap));

		return responses;
	}

	private List<ExpenseGroupResponse> toAdditionalResponses(
		List<Expense> expenses,
		Map<Long, List<ExpenseItem>> expenseItemMap
	) {
		return expenses.stream()
			.map(expense -> ExpenseGroupResponse.ofAdditional(
				expense,
				expenseItemMap.getOrDefault(expense.getId(), Collections.emptyList())
			))
			.toList();
	}
}
