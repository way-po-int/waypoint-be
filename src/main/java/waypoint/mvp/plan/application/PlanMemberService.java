package waypoint.mvp.plan.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanMember;
import waypoint.mvp.plan.domain.PlanRole;
import waypoint.mvp.plan.infrastructure.persistence.PlanMemberRepository;
import waypoint.mvp.user.domain.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanMemberService {

	private final PlanMemberRepository planMemberRepository;

	@Transactional
	public void createInitialOwner(Plan plan, User user) {
		PlanMember planMember = PlanMember.create(plan, user, PlanRole.OWNER);
		planMemberRepository.save(planMember);
	}
}
