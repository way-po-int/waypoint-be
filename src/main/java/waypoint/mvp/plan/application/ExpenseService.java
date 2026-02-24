package waypoint.mvp.plan.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.application.dto.request.ExpenseCreateRequest;
import waypoint.mvp.plan.application.dto.request.ExpenseItemUpdateRequest;
import waypoint.mvp.plan.application.dto.response.ExpenseGroupResponse;
import waypoint.mvp.plan.application.dto.response.ExpenseResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.Budget;
import waypoint.mvp.plan.domain.Expense;
import waypoint.mvp.plan.domain.ExpenseItem;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.error.ExpenseError;
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

		return ExpenseGroupResponse.ofAdditional(expense, items);
	}

	@Transactional
	public void relocateExpenses(Long timeBlockId, Long planDayId, TimeBlock prevTimeBlock) {
		expenseRankService.relocate(timeBlockId, planDayId, prevTimeBlock);
	}

	@Transactional
	public ExpenseResponse updateExpenseItems(
		String planExternalId,
		String expenseExternalId,
		List<ExpenseItemUpdateRequest> requests,
		UserPrincipal user
	) {
		Budget budget = budgetQueryService.getBudget(planExternalId);
		planAuthorizer.verifyMember(user, budget.getPlan().getId());

		Expense expense = expenseQueryService.getExpense(expenseExternalId);
		List<ExpenseItem> existingItems = expenseQueryService.getExpenseItems(expense.getId());

		// 요청에 없는 지출 항목 삭제
		deleteUnrequestedItems(existingItems, requests);

		// 지출 항목 추가 & 수정
		List<ExpenseItem> upsertItems = upsertItems(expense, existingItems, requests);

		return ExpenseResponse.of(expense, upsertItems);
	}

	@Transactional
	public void deleteExpense(
		String planExternalId,
		String expenseExternalId,
		UserPrincipal user
	) {
		Budget budget = budgetQueryService.getBudget(planExternalId);
		planAuthorizer.verifyMember(user, budget.getPlan().getId());

		Expense expense = expenseQueryService.getExpense(expenseExternalId);
		if (expense.isAdditionalType()) {
			// 추가 지출은 지출을 삭제
			expenseRepository.delete(expense);
		} else {
			// 그 외의 타입은 지출 항목만 삭제
			expenseItemRepository.deleteAllInBatchByExpense(expense);
		}
	}

	private void deleteUnrequestedItems(
		List<ExpenseItem> existingItems,
		List<ExpenseItemUpdateRequest> requests
	) {
		Set<String> requestedIds = requests.stream()
			.map(ExpenseItemUpdateRequest::expenseItemId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		List<ExpenseItem> toDelete = existingItems.stream()
			.filter(item -> !requestedIds.contains(item.getExternalId()))
			.toList();

		if (!toDelete.isEmpty()) {
			expenseItemRepository.deleteAllInBatch(toDelete);
			existingItems.removeAll(toDelete);
		}
	}

	private List<ExpenseItem> upsertItems(
		Expense expense,
		List<ExpenseItem> existingItems,
		List<ExpenseItemUpdateRequest> requests
	) {
		Map<String, ExpenseItem> existingItemMap = existingItems.stream()
			.collect(Collectors.toMap(ExpenseItem::getExternalId, item -> item));

		List<ExpenseItem> newItems = new ArrayList<>();
		List<ExpenseItem> resultItems = new ArrayList<>();

		for (ExpenseItemUpdateRequest request : requests) {
			String itemId = request.expenseItemId();
			if (itemId == null) {
				// 지출 항목 추가
				ExpenseItem newItem = ExpenseItem.create(expense, request.name(), request.cost());
				newItems.add(newItem);
				resultItems.add(newItem);
			} else if (existingItemMap.containsKey(itemId)) {
				// 지출 항목 수정
				ExpenseItem item = existingItemMap.get(itemId);
				item.update(request.name(), request.cost());
				resultItems.add(item);
			} else {
				throw new BusinessException(ExpenseError.EXPENSE_ITEM_NOT_FOUND);
			}
		}
		expenseItemRepository.saveAll(newItems);
		return resultItems;
	}
}
