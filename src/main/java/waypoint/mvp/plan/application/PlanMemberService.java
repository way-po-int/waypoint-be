package waypoint.mvp.plan.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.auth.ResourceAuthorizer;
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
	private final ResourceAuthorizer planAuthorizer;

	@Transactional
	public void createInitialOwner(Plan plan, User user) {
		PlanMember planMember = PlanMember.create(plan, user, PlanRole.OWNER);
		planMemberRepository.save(planMember);
	}

	@Transactional
	public void addMember(Plan plan, User user) {
		Optional<PlanMember> withdrawnMemberOpt = planMemberRepository.findWithdrawnMember(
			plan.getId(), user.getId());

		if (withdrawnMemberOpt.isPresent()) {
			PlanMember rejoinedMember = withdrawnMemberOpt.get();
			rejoinedMember.rejoin();
			rejoinedMember.updateProfile(user.getNickname(), user.getPicture());
		} else {
			planAuthorizer.checkIfMemberExists(plan.getId(), user.getId());
			PlanMember newMember = PlanMember.create(plan, user, PlanRole.MEMBER);
			planMemberRepository.save(newMember);
		}
		plan.increaseMemberCount();

	}
}
