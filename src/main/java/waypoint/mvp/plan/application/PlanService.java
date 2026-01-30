package waypoint.mvp.plan.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.application.dto.request.PlanCreateRequest;
import waypoint.mvp.plan.application.dto.response.PlanResponse;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.event.PlanCreateEvent;
import waypoint.mvp.plan.error.PlanError;
import waypoint.mvp.plan.infrastructure.persistence.PlanRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanService {

	private final PlanRepository planRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public PlanResponse createPlan(PlanCreateRequest request, UserPrincipal user) {
		Plan plan = Plan.create(request.title(), request.startDate(), request.endDate());
		Plan savedPlan = planRepository.save(plan);

		eventPublisher.publishEvent(
			PlanCreateEvent.of(savedPlan.getId(), user)
		);

		return PlanResponse.from(getPlan(savedPlan.getId()));
	}

	public SliceResponse<PlanResponse> findPlans(UserPrincipal user, Pageable pageable) {
		Slice<PlanResponse> plans = planRepository.findAllByUserId(user.id(), pageable)
			.map(PlanResponse::from);

		return SliceResponse.from(plans);
	}

	public PlanResponse findPlanById(Long planId) {
		Plan plan = getPlan(planId);

		return PlanResponse.from(plan);
	}

	private Plan getPlan(Long planId) {
		return planRepository.findById(planId)
			.orElseThrow(() -> new BusinessException(PlanError.Plan_NOT_FOUND));
	}
}
