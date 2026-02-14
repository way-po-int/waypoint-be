package waypoint.mvp.plan.application;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
import waypoint.mvp.plan.application.dto.response.BlockDetailResponse;
import waypoint.mvp.plan.application.dto.response.BlockResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.error.BlockError;
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

		Map<Long, Block> selectedBlocks = findSelectedBlocks(planId, timeBlocks);
		List<BlockResponse> contents = mapToBlockResponses(timeBlocks, selectedBlocks);

		return SliceResponse.from(timeBlockSlice, contents);
	}

	public BlockDetailResponse findBlockDetail(String planExternalId, String blockId, AuthPrincipal user) {
		Plan plan = getPlanWithAccess(planExternalId, user);

		Block block = getBlock(plan.getId(), blockId);

		return BlockDetailResponse.from(block, toPlaceResponse(block));
	}

	public Block getBlock(Long planId, String blockExternalId) {
		return blockRepository.findByExternalId(planId, blockExternalId)
			.orElseThrow(() -> new BusinessException(BlockError.BLOCK_NOT_FOUND));
	}

	public PlaceResponse toPlaceResponse(Block block) {
		if (block == null || block.getPlace() == null) {
			return null;
		}
		return collectionPlaceQueryService.toPlaceResponse(block.getPlace());
	}

	// ── private ──────────────────────────────────────────────────

	private Plan getPlanWithAccess(String planExternalId, AuthPrincipal user) {
		Plan plan = planService.getPlan(planExternalId);
		planAuthorizer.verifyAccess(user, plan.getId());
		return plan;
	}

	private Map<Long, Block> findSelectedBlocks(Long planId, List<TimeBlock> timeBlocks) {
		List<Long> timeBlockIds = timeBlocks.stream().map(TimeBlock::getId).toList();
		return blockRepository.findAllByTimeBlockIds(planId, timeBlockIds)
			.stream()
			.filter(Block::isSelected)
			.collect(Collectors.toMap(
				block -> block.getTimeBlock().getId(),
				Function.identity(),
				(first, second) -> first
			));
	}

	private List<BlockResponse> mapToBlockResponses(List<TimeBlock> timeBlocks, Map<Long, Block> selectedBlocks) {
		return timeBlocks.stream()
			.map(timeBlock -> toBlockResponse(timeBlock, selectedBlocks.get(timeBlock.getId())))
			.toList();
	}

	private BlockResponse toBlockResponse(TimeBlock timeBlock, Block selectedBlock) {
		if (selectedBlock == null) {
			return new BlockResponse(timeBlock.getExternalId(), timeBlock.getType(), timeBlock.getStartTime(),
				timeBlock.getEndTime(), null);
		}
		return BlockResponse.from(timeBlock, selectedBlock, toPlaceResponse(selectedBlock));
	}
}
