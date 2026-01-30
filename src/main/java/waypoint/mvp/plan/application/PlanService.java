package waypoint.mvp.plan.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.application.dto.request.PlanCreateRequest;
import waypoint.mvp.plan.application.dto.response.PlanResponse;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.error.PlanError;
import waypoint.mvp.plan.infrastructure.persistence.PlanRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanService {

	private final PlanRepository planRepository;

	@Transactional
	public PlanResponse createPlan(PlanCreateRequest request, UserPrincipal user) {
		Plan plan = Plan.create(request.title(), request.startDate(), request.endDate());
		Plan savedPlan = planRepository.save(plan);

		return PlanResponse.from(getPlan(savedPlan.getId()));
	}

	private Plan getPlan(Long planId) {
		return planRepository.findById(planId)
			.orElseThrow(() -> new BusinessException(PlanError.Plan_NOT_FOUND));
	}
}
