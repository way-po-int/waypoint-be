package waypoint.mvp.plan.application;

import java.util.Objects;
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
import waypoint.mvp.plan.error.PlanMemberError;
import waypoint.mvp.plan.infrastructure.persistence.PlanMemberRepository;
import waypoint.mvp.user.domain.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanMemberService {

	private final PlanMemberRepository planMemberRepository;
	private final ResourceAuthorizer planAuthorizer;

	public boolean isSameMember(PlanMember member, PlanMember other) {
		if (member == null || other == null) {
			return false;
		}
		return Objects.equals(member.getId(), other.getId());
	}

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

	public PlanMember findMember(Long planId, Long memberId) {
		return planMemberRepository.findActive(planId, memberId)
			.orElseThrow(() -> new BusinessException(PlanMemberError.MEMBER_NOT_FOUND));
	}

	public PlanMember findMember(Long planId, String memberExternalId) {
		return planMemberRepository.findActiveByMemberExternalId(planId, memberExternalId)
			.orElseThrow(() -> new BusinessException(PlanMemberError.MEMBER_NOT_FOUND));
	}

	public PlanMember findMemberByUserId(Long planId, Long userId) {
		return planMemberRepository.findActiveByUserId(planId, userId)
			.orElseThrow(() -> new BusinessException(PlanMemberError.MEMBER_NOT_FOUND));
	}

	@Transactional
	public void withdraw(Long planId, UserPrincipal user) {
		PlanMember member = findMemberByUserId(planId, user.getId());
		remove(member);

	}

	@Transactional
	public void expel(Long planId, String memberId, UserPrincipal user) {
		planAuthorizer.verifyOwner(user, planId);
		PlanMember member = findMember(planId, memberId);
		remove(member);
	}

	private void remove(PlanMember member) {
		Plan plan = member.getPlan();
		if (member.isOwner()) {
			throw new BusinessException(PlanError.NEED_TO_DELEGATE_OWNERSHIP);
		} else {
			member.withdraw();
			plan.decreaseMemberCount();
		}
	}
}
