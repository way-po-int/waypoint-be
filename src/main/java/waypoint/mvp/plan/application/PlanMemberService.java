package waypoint.mvp.plan.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanMember;
import waypoint.mvp.plan.domain.PlanRole;
import waypoint.mvp.plan.error.PlanError;
import waypoint.mvp.plan.infrastructure.persistence.PlanMemberRepository;
import waypoint.mvp.plan.infrastructure.persistence.PlanRepository;
import waypoint.mvp.user.domain.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanMemberService {

	private final PlanMemberRepository planMemberRepository;
	private final ResourceAuthorizer planAuthorizer;
	private final PlanRepository planRepository;

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

	public PlanMember getMember(Long planId, Long memberId) {
		return planMemberRepository.findActive(planId, memberId)
			.orElseThrow(() -> new BusinessException(PlanMemberError.MEMBER_NOT_FOUND));
	}

	public PlanMember getMember(Long planId, String memberExternalId) {
		return planMemberRepository.findActiveByMemberExternalId(planId, memberExternalId)
			.orElseThrow(() -> new BusinessException(PlanMemberError.MEMBER_NOT_FOUND));
	}

	public PlanMember getMemberByUserId(Long planId, Long userId) {
		return planMemberRepository.findActiveByUserId(userId, planId)
			.orElseThrow(() -> new BusinessException(PlanMemberError.MEMBER_NOT_FOUND));
	}

	@Transactional
	public void withdraw(Long planId, UserPrincipal user) {
		PlanMember member = getMemberByUserId(planId, user.getId());
		remove(planId, member);

	}

	@Transactional
	public void expel(Long planId, String memberId, UserPrincipal user) {
		planAuthorizer.verifyOwner(user, planId);
		PlanMember member = getMember(planId, memberId);
		remove(planId, member);
	}

	private void remove(Long planId, PlanMember member) {
		Plan plan = planRepository.findById(planId)
			.orElseThrow(() -> new BusinessException(PlanError.PLAN_NOT_FOUND));
		if (member.isOwner()) {
			throw new BusinessException(PlanError.NEED_TO_DELEGATE_OWNERSHIP);
		} else {
			member.withdraw();
			plan.decreaseMemberCount();
		}
	}
}
