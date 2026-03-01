package waypoint.mvp.plan.application.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.application.PlanMemberService;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.event.PlanCreateEvent;
import waypoint.mvp.plan.error.PlanError;
import waypoint.mvp.plan.infrastructure.persistence.PlanRepository;
import waypoint.mvp.user.domain.User;
import waypoint.mvp.user.domain.event.ProfileUpdateEvent;
import waypoint.mvp.user.error.UserError;
import waypoint.mvp.user.infrastructure.persistence.UserRepository;

@Component
@RequiredArgsConstructor
public class PlanEventListener {

	private final PlanRepository planRepository;
	private final UserRepository userRepository;
	private final PlanMemberService planMemberService;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handlePlanCreateEvent(PlanCreateEvent event) {
		Plan plan = planRepository.findById(event.planId())
			.orElseThrow(() -> new BusinessException(PlanError.PLAN_NOT_FOUND));
		User user = userRepository.findById(event.user().id())
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		planMemberService.createInitialOwner(plan, user);
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleProfileUpdateEvent(ProfileUpdateEvent event) {
		planMemberService.updateMemberProfile(event.userId(), event.nickname(), event.picture());
	}
}
