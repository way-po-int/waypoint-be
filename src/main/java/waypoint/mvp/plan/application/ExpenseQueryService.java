package waypoint.mvp.plan.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.domain.Expense;
import waypoint.mvp.plan.domain.ExpenseItem;
import waypoint.mvp.plan.error.ExpenseError;
import waypoint.mvp.plan.infrastructure.persistence.ExpenseItemRepository;
import waypoint.mvp.plan.infrastructure.persistence.ExpenseRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpenseQueryService {

	private final ExpenseRepository expenseRepository;
	private final ExpenseItemRepository expenseItemRepository;

	public Expense getExpense(String externalId) {
		return expenseRepository.findByExternalId(externalId)
			.orElseThrow(() -> new BusinessException(ExpenseError.EXPENSE_NOT_FOUND));
	}

	public Expense getExpenseWithLock(String externalId) {
		return expenseRepository.findByExternalIdWithLock(externalId)
			.orElseThrow(() -> new BusinessException(ExpenseError.EXPENSE_NOT_FOUND));
	}

	public List<ExpenseItem> getExpenseItems(Long expenseId) {
		return expenseItemRepository.findAllByExpenseId(expenseId);
	}
}
