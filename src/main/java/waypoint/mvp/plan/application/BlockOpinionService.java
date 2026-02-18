package waypoint.mvp.plan.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.application.dto.request.BlockOpinionCreateRequest;
import waypoint.mvp.plan.application.dto.response.BlockOpinionResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.BlockOpinion;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanMember;
import waypoint.mvp.plan.error.BlockOpinionError;
import waypoint.mvp.plan.infrastructure.persistence.BlockOpinionRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlockOpinionService {

	private final PlanService planService;
	private final PlanMemberService planMemberService;
	private final BlockQueryService blockQueryService;
	private final ResourceAuthorizer planAuthorizer;

	private final BlockOpinionRepository blockOpinionRepository;

	@Transactional
	public BlockOpinionResponse createOpinion(
		String planExternalId,
		String blockExternalId,
		BlockOpinionCreateRequest request,
		UserPrincipal user
	) {
		Plan plan = planService.getPlan(planExternalId);
		planAuthorizer.verifyMember(user, plan.getId());

		Block block = blockQueryService.getBlock(plan.getId(), blockExternalId);
		PlanMember addedBy = planMemberService.findMemberByUserId(plan.getId(), user.getId());

		if (blockOpinionRepository.existsByBlockIdAndAddedById(block.getId(), addedBy.getId())) {
			throw new BusinessException(BlockOpinionError.BLOCK_OPINION_ALREADY_EXISTS);
		}

		BlockOpinion opinion = BlockOpinion
			.create(block, addedBy, request.type(), request.comment(), request.tagIds());
		blockOpinionRepository.save(opinion);

		return BlockOpinionResponse.from(opinion);
	}
}
