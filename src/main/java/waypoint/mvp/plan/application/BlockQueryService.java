package waypoint.mvp.plan.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.collection.application.CollectionPlaceQueryService;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.dto.PlaceResponse;
import waypoint.mvp.plan.application.dto.BlockAssembly;
import waypoint.mvp.plan.application.dto.BlockOpinionDto;
import waypoint.mvp.plan.application.dto.BlockSliceResult;
import waypoint.mvp.plan.application.dto.response.BlockDetailResponse;
import waypoint.mvp.plan.application.dto.response.BlockOpinionResponse;
import waypoint.mvp.plan.application.dto.response.BlockResponse;
import waypoint.mvp.plan.application.dto.response.CandidateBlockResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanDay;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.domain.error.TimeBlockError;
import waypoint.mvp.plan.error.BlockError;
import waypoint.mvp.plan.error.PlanError;
import waypoint.mvp.plan.infrastructure.persistence.BlockRepository;
import waypoint.mvp.plan.infrastructure.persistence.PlanDayRepository;
import waypoint.mvp.plan.infrastructure.persistence.TimeBlockRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlockQueryService {

	private final PlanService planService;
	private final ResourceAuthorizer planAuthorizer;
	private final TimeBlockRepository timeBlockRepository;
	private final BlockRepository blockRepository;
	private final PlanDayRepository planDayRepository;
	private final BlockOpinionQueryService blockOpinionQueryService;
	private final CollectionPlaceQueryService collectionPlaceQueryService;

	public BlockSliceResult findBlocks(String planExternalId, int day, AuthPrincipal user,
		Pageable pageable) {
		Plan plan = getPlanWithAccess(planExternalId, user);
		Long planId = plan.getId();

		PlanDay planDay = planDayRepository.findByPlanIdAndDay(planId, day)
			.orElseThrow(() -> new BusinessException(PlanError.PLAN_DAY_NOT_FOUND));

		Slice<TimeBlock> timeBlockSlice = timeBlockRepository.findAllByPlanIdAndDay(planId, day, pageable);
		List<TimeBlock> timeBlocks = timeBlockSlice.getContent();

		if (timeBlocks.isEmpty()) {
			return new BlockSliceResult(plan, planDay, timeBlockSlice, List.of());
		}

		Map<Long, List<Block>> blocksByTimeBlockId = findBlocksByTimeBlock(planId, timeBlocks);

		List<Long> allBlockIds = blocksByTimeBlockId.values().stream()
			.flatMap(List::stream)
			.map(Block::getId)
			.toList();
		BlockOpinionDto blockOpinionDto = blockOpinionQueryService.findByBlockIds(allBlockIds);

		Long userId = user.getId();
		List<BlockResponse> contents = mapToBlockResponses(timeBlocks, blocksByTimeBlockId, blockOpinionDto, userId);

		return new BlockSliceResult(plan, planDay, timeBlockSlice, contents);
	}

	public BlockDetailResponse findBlockDetail(String planExternalId, String blockId, AuthPrincipal user) {
		Plan plan = getPlanWithAccess(planExternalId, user);
		Long planId = plan.getId();

		Block block = getBlock(planId, blockId);

		return toBlockDetailResponse(block, plan, user.getId());
	}

	public BlockDetailResponse toBlockDetailResponse(Block block, Plan plan, Long userId) {
		BlockOpinionDto blockOpinionDto = blockOpinionQueryService.findByBlockIds(List.of(block.getId()));
		List<BlockOpinionResponse> opinions = blockOpinionDto.getOpinions(block.getId()).stream()
			.map(BlockOpinionResponse::from)
			.toList();

		CandidateBlockResponse candidateBlock = CandidateBlockResponse.from(
			block, toPlaceResponse(block), blockOpinionDto.getOpinions(block.getId()), userId);

		return BlockDetailResponse.from(block, plan, opinions, candidateBlock);
	}

	public List<Block> getBlocksByTimeBlock(Long planId, TimeBlock timeBlock) {
		return blockRepository.findAllByTimeBlockIds(planId, List.of(timeBlock.getId()));
	}

	public Block getBlock(Long planId, String blockExternalId) {
		return blockRepository.findByExternalId(planId, blockExternalId)
			.orElseThrow(() -> new BusinessException(BlockError.BLOCK_NOT_FOUND));
	}

	private PlaceResponse toPlaceResponse(Block block) {
		if (block == null) {
			return null;
		}

		if (block.getManualPlace() != null) {
			return PlaceResponse.fromManual(block.getManualPlace());
		}

		if (block.getPlace() != null) {
			return collectionPlaceQueryService.toPlaceResponse(block.getPlace());
		}

		return null;
	}

	public TimeBlock getTimeBlock(Long planId, String timeBlockId) {
		return timeBlockRepository.findByExternalId(planId, timeBlockId)
			.orElseThrow(() -> new BusinessException(TimeBlockError.TIME_BLOCK_NOT_FOUND));
	}

	public BlockResponse toBlockResponse(TimeBlock timeBlock, List<Block> candidateBlocks, Long userId) {
		List<Long> candidateBlockIds = candidateBlocks.stream().map(Block::getId).toList();
		BlockOpinionDto blockOpinionDto = blockOpinionQueryService.findByBlockIds(candidateBlockIds);
		return toBlockResponse(timeBlock, candidateBlocks, blockOpinionDto, userId);
	}

	public BlockResponse toBlockResponse(TimeBlock timeBlock, List<Block> candidateBlocks,
		BlockOpinionDto candidateOpinionDto, Long userId) {
		BlockAssembly assembly = BlockAssembly.of(timeBlock, candidateBlocks, candidateOpinionDto,
			this::toPlaceResponse, userId);
		return BlockResponse.from(timeBlock, assembly.status(), assembly.candidates(), assembly.selectedBlock());
	}

	private Plan getPlanWithAccess(String planExternalId, AuthPrincipal user) {
		Plan plan = planService.getPlan(planExternalId);
		planAuthorizer.verifyAccess(user, plan.getId());
		return plan;
	}

	private Map<Long, List<Block>> findBlocksByTimeBlock(Long planId, List<TimeBlock> timeBlocks) {
		List<Long> timeBlockIds = timeBlocks.stream().map(TimeBlock::getId).toList();
		return blockRepository.findAllByTimeBlockIds(planId, timeBlockIds)
			.stream()
			.collect(Collectors.groupingBy(block -> block.getTimeBlock().getId()));
	}

	private List<BlockResponse> mapToBlockResponses(
		List<TimeBlock> timeBlocks, Map<Long, List<Block>> blocksByTimeBlockId, BlockOpinionDto blockOpinionDto,
		Long userId) {
		return timeBlocks.stream()
			.map(timeBlock -> toBlockResponse(
				timeBlock, blocksByTimeBlockId.getOrDefault(timeBlock.getId(), List.of()), blockOpinionDto, userId))
			.toList();
	}
}
