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
import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.dto.PlaceResponse;
import waypoint.mvp.plan.application.dto.BlockOpinionDto;
import waypoint.mvp.plan.application.dto.response.BlockAssembly;
import waypoint.mvp.plan.application.dto.response.BlockDetailResponse;
import waypoint.mvp.plan.application.dto.response.BlockResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.BlockOpinion;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.domain.error.TimeBlockError;
import waypoint.mvp.plan.error.BlockError;
import waypoint.mvp.plan.infrastructure.persistence.BlockOpinionRepository;
import waypoint.mvp.plan.infrastructure.persistence.BlockRepository;
import waypoint.mvp.plan.infrastructure.persistence.TimeBlockRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlockQueryService {

	private final PlanService planService;
	private final ResourceAuthorizer planAuthorizer;
	private final TimeBlockRepository timeBlockRepository;
	private final BlockRepository blockRepository;
	private final BlockOpinionRepository blockOpinionRepository;
	private final CollectionPlaceQueryService collectionPlaceQueryService;

	public SliceResponse<BlockResponse> findBlocks(String planExternalId, int day, AuthPrincipal user,
		Pageable pageable) {
		Plan plan = getPlanWithAccess(planExternalId, user);
		Long planId = plan.getId();

		Slice<TimeBlock> timeBlockSlice = timeBlockRepository.findAllByPlanIdAndDay(planId, day, pageable);
		List<TimeBlock> timeBlocks = timeBlockSlice.getContent();

		if (timeBlocks.isEmpty()) {
			return SliceResponse.from(timeBlockSlice, List.of());
		}

		Map<Long, List<Block>> blocksByTimeBlockId = findBlocksByTimeBlock(planId, timeBlocks);

		List<Long> allBlockIds = blocksByTimeBlockId.values().stream()
			.flatMap(List::stream)
			.map(Block::getId)
			.toList();
		BlockOpinionDto context = createOpinionDTO(allBlockIds);

		List<BlockResponse> contents = mapToBlockResponses(timeBlocks, blocksByTimeBlockId, context);

		return SliceResponse.from(timeBlockSlice, contents);
	}

	public BlockDetailResponse findBlockDetail(String planExternalId, String blockId, AuthPrincipal user) {
		Plan plan = getPlanWithAccess(planExternalId, user);
		Long planId = plan.getId();

		Block block = getBlock(planId, blockId);

		return toBlockDetailResponse(block, planId);
	}

	public BlockDetailResponse toBlockDetailResponse(Block block, Long planId) {
		TimeBlock timeBlock = block.getTimeBlock();
		List<Block> blocks = blockRepository.findAllByTimeBlockIds(planId, List.of(timeBlock.getId()));

		List<Long> blockIds = blocks.stream().map(Block::getId).toList();
		BlockOpinionDto context = createOpinionDTO(blockIds);
		BlockAssembly resolvedBlockDto = BlockAssembly.of(blocks, context, this::toPlaceResponse);

		return BlockDetailResponse.from(block, resolvedBlockDto.status(), resolvedBlockDto.candidates(),
			resolvedBlockDto.selectedBlock());
	}

	public Block getBlock(Long planId, String blockExternalId) {
		return blockRepository.findByExternalId(planId, blockExternalId)
			.orElseThrow(() -> new BusinessException(BlockError.BLOCK_NOT_FOUND));
	}

	private PlaceResponse toPlaceResponse(Block block) {
		if (block == null || block.getPlace() == null) {
			return null;
		}
		return collectionPlaceQueryService.toPlaceResponse(block.getPlace());
	}

	public TimeBlock getTimeBlock(Long planId, String timeBlockId) {
		return timeBlockRepository.findByPlanId(planId, timeBlockId)
			.orElseThrow(() -> new BusinessException(TimeBlockError.TIME_BLOCK_NOT_FOUND));
	}

	public BlockResponse toBlockResponse(TimeBlock timeBlock, List<Block> blocks) {
		List<Long> blockIds = blocks.stream().map(Block::getId).toList();
		BlockOpinionDto context = createOpinionDTO(blockIds);
		return toBlockResponse(timeBlock, blocks, context);
	}

	public BlockResponse toBlockResponse(TimeBlock timeBlock, List<Block> blocks, BlockOpinionDto blockOpinionDto) {
		BlockAssembly assembly = BlockAssembly.of(blocks, blockOpinionDto, this::toPlaceResponse);
		return BlockResponse.from(timeBlock, assembly.status(), assembly.candidates(), assembly.selectedBlock());
	}

	private BlockOpinionDto createOpinionDTO(List<Long> blockIds) {
		if (blockIds.isEmpty()) {
			return new BlockOpinionDto(Map.of());
		}
		Map<Long, List<BlockOpinion>> opinionsByBlockId = blockOpinionRepository.findAllByBlockIds(blockIds)
			.stream()
			.collect(Collectors.groupingBy(opinion -> opinion.getBlock().getId()));
		return new BlockOpinionDto(opinionsByBlockId);
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
		List<TimeBlock> timeBlocks, Map<Long, List<Block>> blocksByTimeBlockId, BlockOpinionDto context) {
		return timeBlocks.stream()
			.map(timeBlock -> toBlockResponse(
				timeBlock, blocksByTimeBlockId.getOrDefault(timeBlock.getId(), List.of()), context))
			.toList();
	}
}
