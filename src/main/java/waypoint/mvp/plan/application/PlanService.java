package waypoint.mvp.plan.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.application.dto.request.PlanCreateRequest;
import waypoint.mvp.plan.application.dto.request.PlanUpdateRequest;
import waypoint.mvp.plan.application.dto.response.PlanResponse;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.event.PlanCreateEvent;
import waypoint.mvp.plan.error.PlanError;
import waypoint.mvp.plan.infrastructure.persistence.PlanRepository;
import waypoint.mvp.sharelink.application.dto.response.ShareLinkResponse;
import waypoint.mvp.sharelink.domain.ShareLink;
import waypoint.mvp.sharelink.error.ShareLinkError;
import waypoint.mvp.sharelink.infrastructure.ShareLinkRepository;
import waypoint.mvp.user.application.UserFinder;
import waypoint.mvp.user.domain.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanService {

	private final PlanRepository planRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final ShareLinkRepository shareLinkRepository;
	private final UserFinder userFinder;
	private final PlanMemberService planMemberService;
	private final ResourceAuthorizer planAuthorizer;

	@Value("${waypoint.invitation.expiration-hours}")
	private long invitationExpirationHours;

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
		planAuthorizer.verifyAccess(user, plan.getId());

		return PlanResponse.from(plan);
	}

	public PlanResponse findPlanByExternalId(String externalId, AuthPrincipal user) {
		Plan plan = getPlan(externalId);
		planAuthorizer.verifyAccess(user, plan.getId());

		return PlanResponse.from(plan);
	}

	@Transactional
	public PlanResponse updatePlan(String planExternalId, PlanUpdateRequest request, UserPrincipal user) {
		Plan plan = getPlan(planExternalId);
		planAuthorizer.verifyMember(user, plan.getId());
		plan.update(request.title(), request.startDate(), request.endDate());

		return PlanResponse.from(plan);
	}

	@Transactional
	public void withdrawPlanMember(String planExternalId, UserPrincipal user) {
		Plan plan = getPlan(planExternalId);

		planMemberService.withdraw(plan.getId(), user);
	}

	@Transactional
	public void expelPlanMember(String planExternalId, String memberExternalId, UserPrincipal user) {
		Plan plan = getPlan(planExternalId);

		planMemberService.expel(plan.getId(), memberExternalId, user);
	}

	@Transactional
	public void deletePlan(String planExternalId, UserPrincipal user) {
		Plan plan = getPlan(planExternalId);
		planAuthorizer.verifyOwner(user, plan.getId());

		planRepository.delete(plan);
	}

	@Transactional
	public ShareLinkResponse createInvitation(String planExternalId, UserPrincipal user) {
		Plan plan = getPlan(planExternalId);
		planAuthorizer.verifyMember(user, plan.getId());
		ShareLink shareLink = ShareLink.create(ShareLink.ShareLinkType.PLAN, plan.getExternalId(), plan.getId(),
			user.getId(),
			invitationExpirationHours);

		shareLinkRepository.save(shareLink);

		return ShareLinkResponse.from(shareLink);
	}

	@Transactional
	public Long addMemberFromShareLink(ShareLink shareLink, Long inviteeUserId) {
		if (shareLink.getTargetType() != ShareLink.ShareLinkType.COLLECTION) {
			throw new BusinessException(ShareLinkError.INVALID_INVITATION_LINK);
		}

		User inviteeUser = userFinder.findById(inviteeUserId);
		Plan plan = getPlan(shareLink.getTargetId());
		planMemberService.addMember(plan, inviteeUser);

		shareLink.increaseUseCount();

		return plan.getId();
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
