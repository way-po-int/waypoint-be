package waypoint.mvp.plan.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.plan.domain.Budget;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.infrastructure.persistence.BudgetRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BudgetService {

	private final BudgetRepository budgetRepository;

	@Transactional
	public void createBudget(Plan plan) {
		Budget budget = Budget.create(plan);
		budgetRepository.save(budget);
	}
}
