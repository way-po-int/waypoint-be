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
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.PlaceService;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.plan.application.dto.BlockCreateCommand;
import waypoint.mvp.plan.application.dto.BlockSliceResult;
import waypoint.mvp.plan.application.dto.request.BlockCreateByPlaceRequest;
import waypoint.mvp.plan.application.dto.request.BlockCreateRequest;
import waypoint.mvp.plan.application.dto.request.BlockUpdateRequest;
import waypoint.mvp.plan.application.dto.request.CandidateBlockCreateRequest;
import waypoint.mvp.plan.application.dto.request.CandidateBlockSelectRequest;
import waypoint.mvp.plan.application.dto.response.BlockDetailResponse;
import waypoint.mvp.plan.application.dto.response.BlockListResponse;
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
	private final BlockQueryService blockQueryService;
	private final ResourceAuthorizer planAuthorizer;
	private final PlanDayRepository planDayRepository;
	private final TimeBlockRepository timeBlockRepository;
	private final BlockRepository blockRepository;
	private final PlanCollectionService planCollectionService;
	private final CollectionPlaceQueryService collectionPlaceQueryService;
	private final PlaceService placeService;

	@Transactional
	public BlockResponse createBlock(String planExternalId, BlockCreateRequest request, UserPrincipal user) {
		return createBlock(planExternalId, request.toCommand(), user);
	}

	@Transactional
	public BlockResponse createBlock(String planExternalId, BlockCreateByPlaceRequest request, UserPrincipal user) {
		return createBlock(planExternalId, request.toCommand(), user);
	}

	private BlockResponse createBlock(String planExternalId, BlockCreateCommand command, UserPrincipal user) {
		Plan plan = getPlanAuthor(planExternalId, user);
		Long planId = plan.getId();

		PlanDay planDay = findPlanDay(planId, command.day());
		TimeBlock timeBlock = saveTimeBlock(planDay, command.startTime(), command.endTime(), command.blockType());
		PlanMember addedBy = planMemberService.findMemberByUserId(planId, user.getId());

		Block block = createBlockByType(planId, command, timeBlock, addedBy);
		block.select();

		return blockQueryService.toBlockResponse(timeBlock, List.of(block), user.getId());
	}

	@Transactional
	public BlockResponse addCandidates(String planExternalId, String timeBlockExternalId,
		CandidateBlockCreateRequest request, UserPrincipal user) {
		Plan plan = getPlanAuthor(planExternalId, user);
		Long planId = plan.getId();

		TimeBlock timeBlock = blockQueryService.getTimeBlock(planId, timeBlockExternalId);
		PlanMember addedBy = planMemberService.findMemberByUserId(planId, user.getId());

		if (!timeBlock.getType().isPlace()) {
			throw new BusinessException(BlockError.CANNOT_ADD_CANDIDATE_TO_FREE_BLOCK);
		}

		List<Block> existingBlocks = blockRepository.findAllByTimeBlockIds(planId, List.of(timeBlock.getId()));
		existingBlocks.stream().filter(Block::isSelected).forEach(Block::unselect);

		List<Block> newBlocks = createCandidateBlocks(planId, request, timeBlock, addedBy);
		blockRepository.saveAll(newBlocks);

		List<Block> allBlocks = blockRepository.findAllByTimeBlockIds(planId, List.of(timeBlock.getId()));
		return blockQueryService.toBlockResponse(timeBlock, allBlocks, user.getId());
	}

	private List<Block> createCandidateBlocks(Long planId, CandidateBlockCreateRequest request, TimeBlock timeBlock,
		PlanMember addedBy) {
		boolean hasCollectionPlaceIds = request.collectionPlaceIds() != null && !request.collectionPlaceIds().isEmpty();

		if (hasCollectionPlaceIds) {
			return createBlocksFromCollectionPlaces(planId, request.collectionPlaceIds(), timeBlock, addedBy);
		}

		return createBlocksFromPlaces(request.placeIds(), timeBlock, addedBy);
	}

	private List<Block> createBlocksFromCollectionPlaces(Long planId, List<String> collectionPlaceIds,
		TimeBlock timeBlock, PlanMember addedBy) {
		List<CollectionPlace> collectionPlaces = collectionPlaceQueryService.getCollectionPlaces(collectionPlaceIds);
		planCollectionService.verifyPlacesLinkedToPlan(planId, collectionPlaces);

		return collectionPlaces.stream()
			.map(cp -> Block.create(cp.getPlace(), cp.getSocialMedia(), timeBlock, null, addedBy))
			.toList();
	}

	private List<Block> createBlocksFromPlaces(List<String> placeIds, TimeBlock timeBlock, PlanMember addedBy) {
		List<Place> places = placeService.getByIds(placeIds);

		return places.stream()
			.map(place -> Block.create(place, null, timeBlock, null, addedBy))
			.toList();
	}

	private TimeBlock saveTimeBlock(PlanDay planDay, LocalTime startTime, LocalTime endTime, TimeBlockType type) {
		TimeBlock timeBlock = TimeBlock.create(planDay, startTime, endTime, type);
		return timeBlockRepository.save(timeBlock);
	}

	private Block createBlockByType(Long planId, BlockCreateCommand command, TimeBlock timeBlock, PlanMember addedBy) {
		if (command.isPlaceBlock()) {
			return createPlaceBlock(planId, command, timeBlock, addedBy);
		}
		return createFreeBlock(command, timeBlock, addedBy);
	}

	private Block createPlaceBlock(Long planId, BlockCreateCommand command, TimeBlock timeBlock, PlanMember addedBy) {
		return switch (command.createType()) {
			case COLLECT_PLACE -> createBlockFromCollectionPlace(planId, timeBlock, command, addedBy);

			case PLACE -> createBlockFromPlace(timeBlock, command, addedBy);

			case MANUAL -> throw new IllegalStateException("MANUAL은 아직 사용하지 못 합니다.");
		};

	}

	private Block createBlockFromCollectionPlace(Long planId, TimeBlock timeBlock, BlockCreateCommand command,
		PlanMember addedBy) {
		CollectionPlace collectionPlace = collectionPlaceQueryService.getCollectionPlace(command.collectionPlaceId());
		planCollectionService.verifyPlacesLinkedToPlan(planId, List.of(collectionPlace));

		return blockRepository.save(
			Block.create(collectionPlace.getPlace(), collectionPlace.getSocialMedia(), timeBlock, command.memo(),
				addedBy)
		);
	}

	private Block createBlockFromPlace(TimeBlock timeBlock, BlockCreateCommand command,
		PlanMember addedBy) {
		Place place = placeService.getById(command.placeId());

		return blockRepository.save(
			Block.create(place, null, timeBlock, command.memo(), addedBy)
		);
	}

	private Block createFreeBlock(BlockCreateCommand command, TimeBlock timeBlock, PlanMember addedBy) {
		return blockRepository.save(Block.createFree(timeBlock, command.memo(), addedBy));
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

	public BlockListResponse findBlocks(String planExternalId, int day, AuthPrincipal user,
		Pageable pageable) {
		BlockSliceResult result = blockQueryService.findBlocks(planExternalId, day, user, pageable);
		return BlockListResponse.from(result.planDay(), result.plan(), result.slice(), result.contents());
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

		return blockQueryService.toBlockDetailResponse(block, plan, user.getId());
	}

	@Transactional
	public BlockResponse fixCandidate(String planExternalId, String timeBlockId, CandidateBlockSelectRequest request,
		UserPrincipal user) {
		Plan plan = getPlanAuthor(planExternalId, user);
		Long planId = plan.getId();

		Block block = blockQueryService.getBlock(planId, request.blockId());
		TimeBlock timeBlock = blockQueryService.getTimeBlock(planId, timeBlockId);
		List<Block> blocks = validateAndGetBlocks(planId, block, timeBlock);

		boolean hasSelected = blocks.stream().anyMatch(Block::isSelected);
		if (hasSelected) {
			throw new BusinessException(BlockError.ALREADY_SELECTED);
		}

		block.select();

		return blockQueryService.toBlockResponse(timeBlock, blocks, user.getId());
	}

	@Transactional
	public BlockResponse unfixCandidate(String planExternalId, String timeBlockId,
		CandidateBlockSelectRequest request,
		UserPrincipal user) {
		Plan plan = getPlanAuthor(planExternalId, user);
		Long planId = plan.getId();

		Block block = blockQueryService.getBlock(planId, request.blockId());
		TimeBlock timeBlock = blockQueryService.getTimeBlock(planId, timeBlockId);
		List<Block> candidateBlocks = validateAndGetBlocks(planId, block, timeBlock);

		if (!block.isSelected()) {
			throw new BusinessException(BlockError.NOT_SELECTED);
		}

		block.unselect();

		return blockQueryService.toBlockResponse(timeBlock, candidateBlocks, user.getId());
	}

	private List<Block> validateAndGetBlocks(Long planId, Block block, TimeBlock timeBlock) {
		if (!block.getTimeBlock().getId().equals(timeBlock.getId())) {
			throw new BusinessException(BlockError.BLOCK_NOT_IN_TIME_BLOCK);
		}
		return blockQueryService.getBlocksByTimeBlock(planId, timeBlock);
	}

	@Transactional
	public void deleteTimeBlock(String planExternalId, String timeBlockId, UserPrincipal user) {
		Plan plan = getPlanAuthor(planExternalId, user);
		Long planId = plan.getId();

		TimeBlock timeBlock = blockQueryService.getTimeBlock(planId, timeBlockId);

		timeBlockRepository.delete(timeBlock);
	}

	@Transactional
	public void deleteBlock(String planExternalId, String timeBlockId, String blockId, UserPrincipal user) {
		Plan plan = getPlanAuthor(planExternalId, user);
		Long planId = plan.getId();

		TimeBlock timeBlock = blockQueryService.getTimeBlock(planId, timeBlockId);
		Block targetBlock = blockQueryService.getBlock(planId, blockId);

		blockRepository.delete(targetBlock);

		// 삭제 후 남은 Block 개수를 다시 조회
		boolean hasBlocks = blockRepository.existsByTimeBlockId(planId, timeBlock.getId());

		if (!hasBlocks) {
			timeBlockRepository.delete(timeBlock);
		}
	}

}
