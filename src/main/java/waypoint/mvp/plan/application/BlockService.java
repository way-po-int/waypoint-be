package waypoint.mvp.plan.application;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
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
import waypoint.mvp.plan.application.dto.request.BlockCreateRequest;
import waypoint.mvp.plan.application.dto.request.BlockUpdateRequest;
import waypoint.mvp.plan.application.dto.request.CandidateBlockCreateRequest;
import waypoint.mvp.plan.application.dto.response.BlockDetailResponse;
import waypoint.mvp.plan.application.dto.response.BlockResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanDay;
import waypoint.mvp.plan.domain.PlanMember;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.domain.TimeBlockType;
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
	private final BlockQueryService blockQueryService;
	private final ResourceAuthorizer planAuthorizer;
	private final PlanDayRepository planDayRepository;
	private final TimeBlockRepository timeBlockRepository;
	private final BlockRepository blockRepository;
	private final PlanCollectionService planCollectionService;
	private final CollectionPlaceQueryService collectionPlaceQueryService;

	@Transactional
	public BlockResponse createBlock(String planExternalId, BlockCreateRequest request, UserPrincipal user) {
		Plan plan = getPlanAuthor(planExternalId, user);
		Long planId = plan.getId();

		PlanDay planDay = findPlanDay(planId, request.day());
		TimeBlock timeBlock = saveTimeBlock(planDay, request.startTime(), request.endTime(), request.type());
		PlanMember addedBy = planMemberService.findMemberByUserId(planId, user.getId());

		Block block = createBlockByType(planId, request, timeBlock, addedBy);

		return blockQueryService.toBlockResponse(timeBlock, List.of(block));
	}

	@Transactional
	public BlockResponse addCandidates(String planExternalId, String timeBlockExternalId,
		CandidateBlockCreateRequest request, UserPrincipal user) {
		Plan plan = getPlanAuthor(planExternalId, user);
		Long planId = plan.getId();

		TimeBlock timeBlock = blockQueryService.getTimeBlock(planId, timeBlockExternalId);
		PlanMember addedBy = planMemberService.findMemberByUserId(planId, user.getId());

		List<CollectionPlace> collectionPlaces = collectionPlaceQueryService.getCollectionPlaces(
			request.collectionPlaceIds());
		planCollectionService.verifyPlacesLinkedToPlan(planId, collectionPlaces);

		List<Block> blocks = collectionPlaces.stream()
			.map(cp -> Block.create(cp.getPlace(), cp.getSocialMedia(), timeBlock, null, addedBy))
			.toList();
		List<Block> savedBlocks = blockRepository.saveAll(blocks);

		return blockQueryService.toBlockResponse(timeBlock, savedBlocks);
	}

	private TimeBlock saveTimeBlock(PlanDay planDay, LocalTime startTime, LocalTime endTime, TimeBlockType type) {
		TimeBlock timeBlock = TimeBlock.create(planDay, startTime, endTime, type);
		return timeBlockRepository.save(timeBlock);
	}

	private Block createBlockByType(Long planId, BlockCreateRequest request, TimeBlock timeBlock, PlanMember addedBy) {
		if (request.type() == TimeBlockType.PLACE) {
			return createPlaceBlock(planId, request, timeBlock, addedBy);
		}
		return createFreeBlock(request, timeBlock, addedBy);
	}

	private Block createPlaceBlock(Long planId, BlockCreateRequest request, TimeBlock timeBlock, PlanMember addedBy) {
		CollectionPlace collectionPlace = collectionPlaceQueryService.getCollectionPlace(request.collectionPlaceId());
		planCollectionService.verifyPlacesLinkedToPlan(planId, List.of(collectionPlace));
		return blockRepository.save(
			Block.create(collectionPlace.getPlace(), collectionPlace.getSocialMedia(), timeBlock, request.memo(),
				addedBy)
		);
	}

	private Block createFreeBlock(BlockCreateRequest request, TimeBlock timeBlock, PlanMember addedBy) {
		return blockRepository.save(Block.createFree(timeBlock, request.memo(), addedBy));
	}

	private PlanDay findPlanDay(Long planId, int day) {
		return planDayRepository.findByPlanIdAndDay(planId, day)
			.orElseThrow(() -> new BusinessException(PlanError.PLAN_DAY_NOT_FOUND));
	}

	private Plan getPlanAuthor(String planExternalId, UserPrincipal user) {
		Plan plan = planService.getPlan(planExternalId);
		planAuthorizer.verifyMember(user, plan.getId());
		return plan;
	}

	public BlockDetailResponse findBlockDetail(String planId, String blockId, AuthPrincipal user) {
		return blockQueryService.findBlockDetail(planId, blockId, user);
	}

	public SliceResponse<BlockResponse> findBlocks(String planExternalId, int day, AuthPrincipal user,
		Pageable pageable) {

		return blockQueryService.findBlocks(planExternalId, day, user, pageable);
	}

	@Transactional
	public BlockDetailResponse updateBlock(String planExternalId, String blockId, BlockUpdateRequest request,
		UserPrincipal user) {
		Plan plan = getPlanAuthor(planExternalId, user);
		Long planId = plan.getId();

		Block block = blockQueryService.getBlock(planId, blockId);
		TimeBlock timeBlock = block.getTimeBlock();

		if (request.day() != null) {
			PlanDay newPlanDay = findPlanDay(planId, request.day());
			timeBlock.updatePlanDay(newPlanDay);
		}

		if (request.startTime() != null && request.endTime() != null) {
			timeBlock.updateTime(request.startTime(), request.endTime());
		}

		if (request.memo() != null) {
			block.updateMemo(request.memo());
		}

		return blockQueryService.toBlockDetailResponse(block, planId);
	}

}
