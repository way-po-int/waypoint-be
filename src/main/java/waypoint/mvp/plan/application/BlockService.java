package waypoint.mvp.plan.application;

import java.time.LocalTime;
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
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.application.CollectionPlaceQueryService;
import waypoint.mvp.collection.domain.CollectionPlace;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.dto.PlaceResponse;
import waypoint.mvp.plan.application.dto.request.BlockCreateRequest;
import waypoint.mvp.plan.application.dto.response.BlockDetailResponse;
import waypoint.mvp.plan.application.dto.response.BlockResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanDay;
import waypoint.mvp.plan.domain.PlanMember;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.domain.TimeBlockType;
import waypoint.mvp.plan.error.BlockError;
import waypoint.mvp.plan.error.PlanError;
import waypoint.mvp.plan.infrastructure.persistence.BlockRepository;
import waypoint.mvp.plan.infrastructure.persistence.PlanDayRepository;
import waypoint.mvp.plan.infrastructure.persistence.TimeBlockRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlockService {

	private final PlanService planService;
	private final PlanMemberService planMemberService;
	private final ResourceAuthorizer planAuthorizer;
	private final PlanDayRepository planDayRepository;
	private final TimeBlockRepository timeBlockRepository;
	private final BlockRepository blockRepository;
	private final CollectionPlaceQueryService collectionPlaceQueryService;

	@Transactional
	public BlockResponse createBlock(String planExternalId, BlockCreateRequest request, UserPrincipal user) {
		Plan plan = getPlanAuthor(planExternalId, user);
		Long planId = plan.getId();

		CollectionPlace collectionPlace = collectionPlaceQueryService.getCollectionPlace(request.collectionPlaceId());
		PlaceResponse placeResponse = collectionPlaceQueryService.toPlaceResponse(collectionPlace);

		PlanDay planDay = findPlanDay(planId, request.day());

		TimeBlock timeBlock = saveTimeBlock(planDay, request.startTime(), request.endTime(), request.type());
		Block block = saveBlock(timeBlock, collectionPlace, planId, request.memo(), user);

		return BlockResponse.from(timeBlock, block, placeResponse);
	}

	private TimeBlock saveTimeBlock(PlanDay planDay, LocalTime startTime, LocalTime endTime, TimeBlockType type) {
		TimeBlock timeBlock = TimeBlock.create(planDay, startTime, endTime, type);

		return timeBlockRepository.save(timeBlock);
	}

	private Block saveBlock(
		TimeBlock timeBlock, CollectionPlace collectionPlace, Long planId,
		String memo, UserPrincipal user
	) {
		PlanMember addedBy = planMemberService.findMemberByUserId(planId, user.getId());
		Block block = Block.create(
			collectionPlace.getPlace(),
			collectionPlace.getSocialMedia(),
			timeBlock,
			memo,
			addedBy
		);

		return blockRepository.save(block);
	}

	public SliceResponse<BlockResponse> findBlocks(String planId, int day, AuthPrincipal user, Pageable pageable) {
		Plan plan = getPlanAuthor(planId, user);

		Slice<TimeBlock> timeBlockSlice = timeBlockRepository.findAllByPlanIdAndDay(plan.getId(), day, pageable);
		List<TimeBlock> timeBlocks = timeBlockSlice.getContent();

		if (timeBlocks.isEmpty()) {
			return SliceResponse.from(timeBlockSlice, List.of());
		}

		List<Long> timeBlockIds = timeBlocks.stream().map(TimeBlock::getId).toList();
		Map<Long, Block> selectedBlockByTimeBlockId = blockRepository.findAllByTimeBlockIds(timeBlockIds)
			.stream()
			.filter(Block::isSelected)
			.collect(Collectors.toMap(block -> block.getTimeBlock().getId(), Function.identity()));

		List<BlockResponse> contents = timeBlocks.stream()
			.map(timeBlock -> toBlockResponse(timeBlock, selectedBlockByTimeBlockId.get(timeBlock.getId())))
			.toList();

		return SliceResponse.from(timeBlockSlice, contents);
	}

	public BlockDetailResponse findBlockDetail(String planId, String blockId, AuthPrincipal user) {
		getPlanAuthor(planId, user);

		Block block = blockRepository.findByExternalIdWithFetch(blockId)
			.orElseThrow(() -> new BusinessException(BlockError.PLAN_NOT_FOUND));
		PlaceResponse placeResponse = collectionPlaceQueryService.toPlaceResponse(block.getPlace());

		return BlockDetailResponse.from(block, placeResponse);
	}

	private BlockResponse toBlockResponse(TimeBlock timeBlock, Block selectedBlock) {
		PlaceResponse placeResponse = collectionPlaceQueryService.toPlaceResponse(selectedBlock.getPlace());

		return BlockResponse.from(timeBlock, selectedBlock, placeResponse);
	}

	private PlanDay findPlanDay(Long planId, int day) {
		return planDayRepository.findByPlanIdAndDay(planId, day)
			.orElseThrow(() -> new BusinessException(PlanError.PLAN_DAY_NOT_FOUND));
	}

	private Plan getPlanAuthor(String planExternalId, AuthPrincipal user) {
		Plan plan = planService.getPlan(planExternalId);
		planAuthorizer.verifyAccess(user, plan.getId());

		return plan;
	}

	private Plan getPlanAuthor(String planExternalId, UserPrincipal user) {
		Plan plan = planService.getPlan(planExternalId);
		planAuthorizer.verifyMember(user, plan.getId());

		return plan;
	}
}
