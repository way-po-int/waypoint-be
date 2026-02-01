package waypoint.mvp.plan.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.application.dto.request.PlanCreateRequest;
import waypoint.mvp.plan.application.dto.request.PlanUpdateRequest;
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

		return PlanResponse.from(savedPlan);
	}

	public SliceResponse<PlanResponse> findPlans(UserPrincipal user, Pageable pageable) {
		Slice<PlanResponse> plans = planRepository.findAllByUserId(user.id(), pageable)
			.map(PlanResponse::from);

		return SliceResponse.from(plans);
	}

	public PlanResponse findPlanById(Long planId, AuthPrincipal user) {
		Plan plan = getPlan(planId);

		return PlanResponse.from(plan);
	}

	public PlanResponse findPlanByExternalId(String externalId, AuthPrincipal user) {
		Plan plan = getPlan(externalId);

		return PlanResponse.from(plan);
	}

	@Transactional
	public PlanResponse updatePlan(String planExternalId, PlanUpdateRequest request, UserPrincipal user) {
		Plan plan = getPlan(planExternalId);
		plan.update(request.title(), request.startDate(), request.endDate());

		return PlanResponse.from(plan);
	}

	@Transactional
	public void deletePlan(String planExternalId, UserPrincipal user) {
		Plan plan = getPlan(planExternalId);

		planRepository.delete(plan);
	}

	private Plan getPlan(Long planId) {
		return planRepository.findById(planId)
			.orElseThrow(() -> new BusinessException(PlanError.PLAN_NOT_FOUND));
	}

	private Plan getPlan(String externalId) {
		return planRepository.findByExternalId(externalId)
			.orElseThrow(() -> new BusinessException(PlanError.PLAN_NOT_FOUND));
	}
}
