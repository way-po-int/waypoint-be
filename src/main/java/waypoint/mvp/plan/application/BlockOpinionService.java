package waypoint.mvp.plan.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
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

		// 블록 타입이 장소 블록이 아닌 경우 예외 발생
		if (!block.getTimeBlock().getType().isPlace()) {
			throw new BusinessException(BlockOpinionError.BLOCK_NOT_PLACE_TYPE);
		}

		// 이미 해당 블록에 대한 의견이 작성된 경우 예외 발생
		if (blockOpinionRepository.existsByBlockIdAndAddedById(block.getId(), addedBy.getId())) {
			throw new BusinessException(BlockOpinionError.BLOCK_OPINION_ALREADY_EXISTS);
		}

		BlockOpinion opinion = BlockOpinion
			.create(block, addedBy, request.type(), request.comment(), request.tagIds());
		blockOpinionRepository.save(opinion);

		return BlockOpinionResponse.from(opinion);
	}

	public List<BlockOpinionResponse> findOpinions(
		String planExternalId,
		String blockExternalId,
		AuthPrincipal user
	) {
		Plan plan = planService.getPlan(planExternalId);
		planAuthorizer.verifyAccess(user, plan.getId());

		Block block = blockQueryService.getBlock(plan.getId(), blockExternalId);
		List<BlockOpinion> opinions = blockOpinionRepository.findAllByBlockId(block.getId());

		return opinions.stream()
			.map(BlockOpinionResponse::from)
			.toList();
	}

	public BlockOpinionResponse findOpinion(
		String planExternalId,
		String blockExternalId,
		String opinionExternalId,
		AuthPrincipal user
	) {
		Plan plan = planService.getPlan(planExternalId);
		planAuthorizer.verifyAccess(user, plan.getId());

		Block block = blockQueryService.getBlock(plan.getId(), blockExternalId);
		BlockOpinion opinion = getOpinion(block.getId(), opinionExternalId);

		return BlockOpinionResponse.from(opinion);
	}

	private BlockOpinion getOpinion(Long blockId, String opinionExternalId) {
		return blockOpinionRepository.findByBlockIdAndExternalId(blockId, opinionExternalId)
			.orElseThrow(() -> new BusinessException(BlockOpinionError.BLOCK_OPINION_NOT_FOUND));
	}
}
