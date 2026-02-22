package waypoint.mvp.plan.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.domain.Budget;
import waypoint.mvp.plan.error.BudgetError;
import waypoint.mvp.plan.infrastructure.persistence.BudgetRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BudgetQueryService {

	private final BudgetRepository budgetRepository;

	public Budget getBudget(Long planId) {
		return budgetRepository.findByPlanId(planId)
			.orElseThrow(() -> new BusinessException(BudgetError.BUDGET_NOT_FOUND));
	}

	public Budget getBudget(String planExternalId) {
		return budgetRepository.findByPlanExternalId(planExternalId)
			.orElseThrow(() -> new BusinessException(BudgetError.BUDGET_NOT_FOUND));
	}
}
