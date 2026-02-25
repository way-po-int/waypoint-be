package waypoint.mvp.plan.application;

import static org.assertj.core.api.Assertions.*;
import static waypoint.mvp.plan.application.assertion.BlockResponseAssert.*;
import static waypoint.mvp.plan.application.assertion.CandidateBlockResponseAssert.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;

import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionPlace;
import waypoint.mvp.collection.domain.CollectionRole;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.annotation.ServiceTest;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.PlaceDetail;
import waypoint.mvp.place.infrastructure.persistence.PlaceRepository;
import waypoint.mvp.plan.application.dto.request.BlockCreateRequest;
import waypoint.mvp.plan.application.dto.request.CandidateBlockCreateRequest;
import waypoint.mvp.plan.application.dto.request.CandidateBlockSelectRequest;
import waypoint.mvp.plan.application.dto.response.BlockResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.BlockStatus;
import waypoint.mvp.plan.domain.Budget;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanCollection;
import waypoint.mvp.plan.domain.PlanDay;
import waypoint.mvp.plan.domain.PlanMember;
import waypoint.mvp.plan.domain.PlanRole;
import waypoint.mvp.plan.domain.TimeBlockType;
import waypoint.mvp.plan.error.BlockError;
import waypoint.mvp.plan.infrastructure.persistence.BlockRepository;
import waypoint.mvp.plan.infrastructure.persistence.BudgetRepository;
import waypoint.mvp.plan.infrastructure.persistence.PlanCollectionRepository;
import waypoint.mvp.plan.infrastructure.persistence.PlanDayRepository;
import waypoint.mvp.plan.infrastructure.persistence.PlanMemberRepository;
import waypoint.mvp.plan.infrastructure.persistence.PlanRepository;
import waypoint.mvp.plan.infrastructure.persistence.TimeBlockRepository;
import waypoint.mvp.user.domain.Provider;
import waypoint.mvp.user.domain.SocialAccount;
import waypoint.mvp.user.domain.User;
import waypoint.mvp.user.infrastructure.persistence.UserRepository;

@ServiceTest
class BlockServiceTest {

	@Autowired
	private BlockService blockService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PlanRepository planRepository;

	@Autowired
	private PlanMemberRepository planMemberRepository;

	@Autowired
	private PlanDayRepository planDayRepository;

	@Autowired
	private BlockRepository blockRepository;

	@Autowired
	private PlaceRepository placeRepository;

	@Autowired
	private CollectionRepository collectionRepository;

	@Autowired
	private CollectionMemberRepository collectionMemberRepository;

	@Autowired
	private CollectionPlaceRepository collectionPlaceRepository;

	@Autowired
	private PlanCollectionRepository planCollectionRepository;

	@Autowired
	private TimeBlockRepository timeBlockRepository;

	@Autowired
	private BudgetRepository budgetRepository;

	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
	private static final LocalTime DEFAULT_START = LocalTime.of(10, 0);
	private static final LocalTime DEFAULT_END = LocalTime.of(12, 0);
	private static final int DEFAULT_DAY = 1;

	private User user;
	private UserPrincipal userPrincipal;
	private Plan plan;
	private PlanDay planDay;
	private PlanMember planMember;

	@BeforeEach
	void setUp() {
		SocialAccount socialAccount = SocialAccount.create(Provider.GOOGLE, "test-google-id");
		user = userRepository.save(User.create(socialAccount, "tester", "pic.png", "test@test.com"));
		userPrincipal = new UserPrincipal(user.getId());

		plan = planRepository.save(Plan.create("Test Plan", LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 5)));
		planMember = planMemberRepository.save(PlanMember.create(plan, user, PlanRole.OWNER));
		planDay = planDayRepository.save(PlanDay.create(plan, DEFAULT_DAY));
		budgetRepository.save(Budget.create(plan));
	}

	// -- Fixture Helpers --

	private CollectionPlace createCollectionPlace(String placeName) {
		Collection collection = collectionRepository.save(Collection.create("Test Collection"));
		CollectionMember collectionMember = collectionMemberRepository.save(
			CollectionMember.create(collection, user, CollectionRole.OWNER));

		Place place = placeRepository.save(
			Place.create(placeName, "서울시 강남구", GEOMETRY_FACTORY.createPoint(new Coordinate(127.0, 37.5)),
				PlaceDetail.create("google-place-id-" + System.nanoTime()), 1L));

		CollectionPlace collectionPlace = collectionPlaceRepository.save(
			CollectionPlace.create(collection, place, collectionMember));

		planCollectionRepository.save(PlanCollection.create(plan, collection, planMember));

		return collectionPlace;
	}

	// -- Request Builders -----------

	private BlockCreateRequest placeBlockRequest(String collectionPlaceId, String memo) {
		return new BlockCreateRequest(collectionPlaceId, TimeBlockType.PLACE, DEFAULT_DAY, DEFAULT_START, DEFAULT_END,
			memo);
	}

	private BlockCreateRequest freeBlockRequest(String memo) {
		return new BlockCreateRequest(null, TimeBlockType.FREE, DEFAULT_DAY, DEFAULT_START, DEFAULT_END, memo);
	}

	// -- Service Call Helpers -----------

	private BlockResponse createBlock(BlockCreateRequest request) {
		return blockService.createBlock(plan.getExternalId(), request, userPrincipal);
	}

	private BlockResponse addCandidates(String timeBlockExternalId, List<String> collectionPlaceIds) {
		CandidateBlockCreateRequest request = CandidateBlockCreateRequest.createCollectionPlaceIds(collectionPlaceIds);
		return blockService.addCandidates(plan.getExternalId(), timeBlockExternalId, request, userPrincipal);
	}

	private BlockResponse selectCandidate(String timeBlockExternalId, String blockId) {
		CandidateBlockSelectRequest request = new CandidateBlockSelectRequest(blockId);
		return blockService.updateCandidateSelection(plan.getExternalId(), timeBlockExternalId, request, true,
			userPrincipal);
	}

	private BlockResponse unselectCandidate(String timeBlockExternalId, String blockId) {
		CandidateBlockSelectRequest request = new CandidateBlockSelectRequest(blockId);
		return blockService.updateCandidateSelection(plan.getExternalId(), timeBlockExternalId, request, false,
			userPrincipal);
	}

	// -- DB Helpers --

	private List<Block> findBlocksByTimeBlockExternalId(String timeBlockExternalId) {
		Long timeBlockId = timeBlockRepository.findByExternalId(plan.getId(), timeBlockExternalId)
			.orElseThrow()
			.getId();
		return blockRepository.findAllByTimeBlockIds(plan.getId(), List.of(timeBlockId));
	}

	@Nested
	@DisplayName("블록 추가")
	class CreateBlock {

		@Test
		@DisplayName("PLACE 블록 생성 시 selected=true, blockStatus=DIRECT")
		void createPlaceBlock_selectedTrue_statusDirect() {
			// given
			CollectionPlace cp = createCollectionPlace("강남 맛집");
			BlockCreateRequest request = placeBlockRequest(cp.getExternalId(), "점심 예약 필요");

			// when
			BlockResponse response = createBlock(request);

			// then
			assertThatBlock(response).hasStatus(BlockStatus.DIRECT)
				.hasType(TimeBlockType.PLACE)
				.hasCandidateCount(1)
				.hasStartTime(DEFAULT_START)
				.hasEndTime(DEFAULT_END)
				.hasSelectedBlockWithMemo("점심 예약 필요")
				.hasSelectedBlockWithPlaceName("강남 맛집")
				.hasSelectedBlockAddedBy("tester");

			assertThatCandidate(response.selectedBlock()).isSelected();

			// DB 검증
			List<Block> blocks = findBlocksByTimeBlockExternalId(response.timeBlockId());
			assertThat(blocks).hasSize(1);
			assertThat(blocks.get(0).isSelected()).isTrue();
		}

		@Test
		@DisplayName("FREE 블록 생성 시 selected=true, blockStatus=NOTHING")
		void createFreeBlock_selectedTrue_statusNothing() {
			// given
			BlockCreateRequest request = freeBlockRequest("카페에서 휴식");

			// when
			BlockResponse response = createBlock(request);

			// then
			assertThatBlock(response).hasStatus(BlockStatus.NOTHING)
				.hasType(TimeBlockType.FREE)
				.hasCandidateCount(1)
				.hasStartTime(DEFAULT_START)
				.hasEndTime(DEFAULT_END);

			assertThat(response.candidates()).hasSize(1);
			assertThatCandidate(response.candidates().get(0)).hasMemo("카페에서 휴식")
				.hasNoPlace()
				.hasAddedByNickname("tester");
		}
	}

	@Nested
	@DisplayName("후보지 추가")
	class AddCandidates {

		@Test
		@DisplayName("후보지 추가 시 기존 블록 unselect, 전체 blockStatus=PENDING")
		void addCandidates_allUnselected_statusPending() {
			// given
			CollectionPlace cp1 = createCollectionPlace("홍대 카페");
			BlockResponse createResponse = createBlock(placeBlockRequest(cp1.getExternalId(), null));
			assertThat(createResponse.blockStatus()).isEqualTo(BlockStatus.DIRECT);

			CollectionPlace cp2 = createCollectionPlace("이태원 레스토랑");

			// when
			BlockResponse response = addCandidates(createResponse.timeBlockId(), List.of(cp2.getExternalId()));

			// then
			assertThatBlock(response).hasStatus(BlockStatus.PENDING)
				.hasNoSelectedBlock()
				.hasCandidateCount(2)
				.allCandidatesUnselected()
				.hasCandidatesWithPlaceNames("홍대 카페", "이태원 레스토랑")
				.allCandidatesAddedBy("tester");
		}
	}

	@Nested
	@DisplayName("후보지 확정")
	class SelectCandidate {

		@Test
		@DisplayName("후보지 중 하나를 확정하면 blockStatus=FIXED")
		void selectCandidate_statusFixed() {
			// given
			CollectionPlace cp1 = createCollectionPlace("명동 쇼핑");
			BlockResponse createResponse = createBlock(placeBlockRequest(cp1.getExternalId(), null));

			CollectionPlace cp2 = createCollectionPlace("종로 서점");
			BlockResponse pendingResponse = addCandidates(createResponse.timeBlockId(), List.of(cp2.getExternalId()));

			// 확정 전 상태 검증
			assertThatBlock(pendingResponse).hasStatus(BlockStatus.PENDING)
				.hasNoSelectedBlock()
				.allCandidatesUnselected();

			// when - 첫 번째 후보지 확정
			String blockIdToSelect = pendingResponse.candidates().get(0).blockId();
			BlockResponse response = selectCandidate(pendingResponse.timeBlockId(), blockIdToSelect);

			// then - 확정 후 상태 검증
			assertThatBlock(response).hasStatus(BlockStatus.FIXED).hasSelectedBlock().hasCandidateCount(2);

			assertThatCandidate(response.selectedBlock()).isSelected();

			// DB 검증
			List<Block> blocks = findBlocksByTimeBlockExternalId(response.timeBlockId());
			long selectedCount = blocks.stream().filter(Block::isSelected).count();
			assertThat(selectedCount).isEqualTo(1);
		}

	}

	@Nested
	@DisplayName("후보지 확정 취소")
	class UnselectCandidate {

		@Test
		@DisplayName("확정된 후보지를 취소하면 blockStatus=PENDING으로 변경")
		void unselectCandidate_statusPending() {
			// given - 후보지 2개 생성 후 하나 확정
			CollectionPlace cp1 = createCollectionPlace("신촌 맛집");
			CollectionPlace cp2 = createCollectionPlace("홍대 맛집");

			BlockResponse createResponse = createBlock(placeBlockRequest(cp1.getExternalId(), null));
			BlockResponse pendingResponse = addCandidates(createResponse.timeBlockId(), List.of(cp2.getExternalId()));

			String blockIdToSelect = pendingResponse.candidates().get(0).blockId();
			BlockResponse selectedResponse = selectCandidate(pendingResponse.timeBlockId(), blockIdToSelect);

			// 확정 상태 검증
			assertThatBlock(selectedResponse).hasStatus(BlockStatus.FIXED).hasSelectedBlock();

			assertThatCandidate(selectedResponse.selectedBlock()).isSelected();

			// when - 확정 취소
			BlockResponse response = unselectCandidate(selectedResponse.timeBlockId(),
				selectedResponse.selectedBlock().blockId());

			// then - 취소 후 상태 검증
			assertThatBlock(response).hasStatus(BlockStatus.PENDING)
				.hasNoSelectedBlock()
				.allCandidatesUnselected()
				.hasCandidateCount(2);

			// DB 검증
			List<Block> blocks = findBlocksByTimeBlockExternalId(response.timeBlockId());
			boolean anySelected = blocks.stream().anyMatch(Block::isSelected);
			assertThat(anySelected).isFalse();
		}

		@Test
		@DisplayName("확정되지 않은 후보지를 취소하면 예외 발생")
		void unselectCandidate_notSelected_throwsException() {
			// given
			CollectionPlace cp1 = createCollectionPlace("서울역 카페");
			CollectionPlace cp2 = createCollectionPlace("용산역 카페");

			BlockResponse createResponse = createBlock(placeBlockRequest(cp1.getExternalId(), null));
			BlockResponse pendingResponse = addCandidates(createResponse.timeBlockId(), List.of(cp2.getExternalId()));

			// 확정하지 않은 상태
			assertThatBlock(pendingResponse).hasStatus(BlockStatus.PENDING).allCandidatesUnselected();

			// when & then - 확정되지 않은 후보지 취소 시도 시 예외 발생
			String blockId = pendingResponse.candidates().get(0).blockId();
			assertThatThrownBy(() -> unselectCandidate(pendingResponse.timeBlockId(), blockId)).hasMessageContaining(
				BlockError.NOT_SELECTED.getMessage());
		}

		@Test
		@DisplayName("확정 후 취소 후 다시 확정하면 blockStatus=FIXED")
		void selectAfterUnselect_statusFixed() {
			// given - 후보지 생성 및 확정
			CollectionPlace cp1 = createCollectionPlace("판교 레스토랑");
			CollectionPlace cp2 = createCollectionPlace("분당 레스토랑");

			BlockResponse createResponse = createBlock(placeBlockRequest(cp1.getExternalId(), null));
			BlockResponse pendingResponse = addCandidates(createResponse.timeBlockId(), List.of(cp2.getExternalId()));

			String firstBlockId = pendingResponse.candidates().get(0).blockId();
			BlockResponse selectedResponse = selectCandidate(pendingResponse.timeBlockId(), firstBlockId);

			// 확정 취소
			BlockResponse unselectedResponse = unselectCandidate(selectedResponse.timeBlockId(),
				selectedResponse.selectedBlock().blockId());

			assertThatBlock(unselectedResponse).hasStatus(BlockStatus.PENDING).hasNoSelectedBlock();

			// when - 다른 후보지 확정
			String secondBlockId = unselectedResponse.candidates().get(1).blockId();
			BlockResponse reselectedResponse = selectCandidate(unselectedResponse.timeBlockId(), secondBlockId);

			// then - 다시 FIXED 상태
			assertThatBlock(reselectedResponse).hasStatus(BlockStatus.FIXED).hasSelectedBlock();

			assertThatCandidate(reselectedResponse.selectedBlock()).isSelected();

			// 선택된 블록이 두 번째 블록인지 확인
			assertThat(reselectedResponse.selectedBlock().blockId()).isEqualTo(secondBlockId);
		}
	}

	@Nested
	@DisplayName("TimeBlock 삭제")
	class DeleteTimeBlock {

		@Test
		@DisplayName("TimeBlock 삭제 시 연관된 모든 Block들이 함께 삭제된다")
		void deleteTimeBlock_deletesAllRelatedBlocks() {
			// given - 후보지 3개가 있는 TimeBlock 생성
			CollectionPlace cp1 = createCollectionPlace("강남 카페");
			CollectionPlace cp2 = createCollectionPlace("역삼 카페");
			CollectionPlace cp3 = createCollectionPlace("선릉 카페");

			BlockResponse createResponse = createBlock(placeBlockRequest(cp1.getExternalId(), "메모1"));
			String timeBlockId = createResponse.timeBlockId();

			addCandidates(timeBlockId, List.of(cp2.getExternalId(), cp3.getExternalId()));

			// 삭제 전 Block 개수 확인
			List<Block> blocksBeforeDelete = findBlocksByTimeBlockExternalId(timeBlockId);
			assertThat(blocksBeforeDelete).hasSize(3);

			// when - TimeBlock 삭제
			blockService.deleteTimeBlock(plan.getExternalId(), timeBlockId, userPrincipal);

			// then - TimeBlock과 연관된 모든 Block이 삭제됨
			assertThat(timeBlockRepository.findByExternalId(plan.getId(), timeBlockId)).isEmpty();

			Long timeBlockInternalId = blocksBeforeDelete.get(0).getTimeBlock().getId();
			List<Block> blocksAfterDelete = blockRepository.findAllByTimeBlockIds(plan.getId(),
				List.of(timeBlockInternalId));
			assertThat(blocksAfterDelete).isEmpty();
		}

		@Test
		@DisplayName("FREE 타입 TimeBlock 삭제 시 정상 동작한다")
		void deleteTimeBlock_freeType_success() {
			// given - FREE 타입 블록 생성
			BlockResponse createResponse = createBlock(freeBlockRequest("자유 시간"));
			String timeBlockId = createResponse.timeBlockId();

			// 삭제 전 존재 확인
			assertThat(timeBlockRepository.findByExternalId(plan.getId(), timeBlockId)).isPresent();

			// when - TimeBlock 삭제
			blockService.deleteTimeBlock(plan.getExternalId(), timeBlockId, userPrincipal);

			// then - TimeBlock과 Block이 삭제됨
			assertThat(timeBlockRepository.findByExternalId(plan.getId(), timeBlockId)).isEmpty();
		}

		@Test
		@DisplayName("선택된 후보지가 있는 TimeBlock도 삭제 가능하다")
		void deleteTimeBlock_withSelectedBlock_success() {
			// given - 후보지 생성 및 확정
			CollectionPlace cp1 = createCollectionPlace("명동 맛집");
			CollectionPlace cp2 = createCollectionPlace("종로 맛집");

			BlockResponse createResponse = createBlock(placeBlockRequest(cp1.getExternalId(), null));
			String timeBlockId = createResponse.timeBlockId();

			BlockResponse pendingResponse = addCandidates(timeBlockId, List.of(cp2.getExternalId()));
			String blockIdToSelect = pendingResponse.candidates().get(0).blockId();
			selectCandidate(timeBlockId, blockIdToSelect);

			// when - TimeBlock 삭제
			blockService.deleteTimeBlock(plan.getExternalId(), timeBlockId, userPrincipal);

			// then - 삭제 성공
			assertThat(timeBlockRepository.findByExternalId(plan.getId(), timeBlockId)).isEmpty();
		}
	}

	@Nested
	@DisplayName("후보지(Block) 삭제")
	class DeleteBlock {

		@Test
		@DisplayName("TimeBlock에 여러 Block이 있을 때 특정 Block만 삭제되고 TimeBlock은 유지된다")
		void deleteBlock_multipleBlocks_onlyTargetBlockDeleted() {
			// given - 후보지 3개 생성
			CollectionPlace cp1 = createCollectionPlace("홍대 카페");
			CollectionPlace cp2 = createCollectionPlace("신촌 카페");
			CollectionPlace cp3 = createCollectionPlace("이대 카페");

			BlockResponse createResponse = createBlock(placeBlockRequest(cp1.getExternalId(), null));
			String timeBlockId = createResponse.timeBlockId();

			BlockResponse candidatesResponse = addCandidates(timeBlockId,
				List.of(cp2.getExternalId(), cp3.getExternalId()));

			// 삭제 전 Block 개수 확인
			List<Block> blocksBeforeDelete = findBlocksByTimeBlockExternalId(timeBlockId);
			assertThat(blocksBeforeDelete).hasSize(3);

			// when - 첫 번째 후보지 삭제
			String blockIdToDelete = candidatesResponse.candidates().get(0).blockId();
			blockService.deleteBlock(plan.getExternalId(), timeBlockId, blockIdToDelete, userPrincipal);

			// then - 해당 Block만 삭제되고 TimeBlock은 유지됨
			assertThat(timeBlockRepository.findByExternalId(plan.getId(), timeBlockId)).isPresent();

			List<Block> blocksAfterDelete = findBlocksByTimeBlockExternalId(timeBlockId);
			assertThat(blocksAfterDelete).hasSize(2);

			// 삭제된 Block이 목록에 없는지 확인
			boolean deletedBlockExists = blocksAfterDelete.stream()
				.anyMatch(block -> block.getExternalId().equals(blockIdToDelete));
			assertThat(deletedBlockExists).isFalse();
		}

		@Test
		@DisplayName("TimeBlock에 Block이 하나만 있을 때 해당 Block 삭제 시 TimeBlock도 함께 삭제된다")
		void deleteBlock_singleBlock_timeBlockAlsoDeleted() {
			// given - Block 1개만 있는 TimeBlock 생성
			CollectionPlace cp = createCollectionPlace("강남역 맛집");
			BlockResponse createResponse = createBlock(placeBlockRequest(cp.getExternalId(), "점심"));
			String timeBlockId = createResponse.timeBlockId();
			String blockId = createResponse.selectedBlock().blockId();

			// 삭제 전 확인
			assertThat(timeBlockRepository.findByExternalId(plan.getId(), timeBlockId)).isPresent();
			List<Block> blocksBeforeDelete = findBlocksByTimeBlockExternalId(timeBlockId);
			assertThat(blocksBeforeDelete).hasSize(1);

			// when - 유일한 Block 삭제
			blockService.deleteBlock(plan.getExternalId(), timeBlockId, blockId, userPrincipal);

			// then - TimeBlock도 함께 삭제됨
			assertThat(timeBlockRepository.findByExternalId(plan.getId(), timeBlockId)).isEmpty();
		}

		@Test
		@DisplayName("FREE 타입 Block 삭제 시 TimeBlock도 함께 삭제된다")
		void deleteBlock_freeType_timeBlockAlsoDeleted() {
			// given - FREE 타입 블록 생성 (항상 1개만 존재)
			BlockResponse createResponse = createBlock(freeBlockRequest("휴식 시간"));
			String timeBlockId = createResponse.timeBlockId();
			String blockId = createResponse.candidates().get(0).blockId();

			// when - Block 삭제
			blockService.deleteBlock(plan.getExternalId(), timeBlockId, blockId, userPrincipal);

			// then - TimeBlock도 함께 삭제됨
			assertThat(timeBlockRepository.findByExternalId(plan.getId(), timeBlockId)).isEmpty();
		}

		@Test
		@DisplayName("존재하지 않는 Block 삭제 시도 시 예외가 발생한다")
		void deleteBlock_nonExistentBlock_throwsException() {
			// given - 정상 Block 생성
			CollectionPlace cp = createCollectionPlace("서울역 카페");
			BlockResponse createResponse = createBlock(placeBlockRequest(cp.getExternalId(), null));
			String timeBlockId = createResponse.timeBlockId();

			String nonExistentBlockId = "non-existent-block-id";

			// when & then - 존재하지 않는 Block 삭제 시도 시 예외 발생
			assertThatThrownBy(() -> blockService.deleteBlock(plan.getExternalId(), timeBlockId, nonExistentBlockId,
				userPrincipal)).isInstanceOf(BusinessException.class)
				.hasMessageContaining(BlockError.BLOCK_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("선택된 후보지를 삭제할 수 있다")
		void deleteBlock_selectedBlock_success() {
			// given - 후보지 2개 생성 및 하나 확정
			CollectionPlace cp1 = createCollectionPlace("판교 레스토랑");
			CollectionPlace cp2 = createCollectionPlace("분당 레스토랑");

			BlockResponse createResponse = createBlock(placeBlockRequest(cp1.getExternalId(), null));
			String timeBlockId = createResponse.timeBlockId();

			BlockResponse pendingResponse = addCandidates(timeBlockId, List.of(cp2.getExternalId()));
			String selectedBlockId = pendingResponse.candidates().get(0).blockId();
			selectCandidate(timeBlockId, selectedBlockId);

			// when - 선택된 Block 삭제
			blockService.deleteBlock(plan.getExternalId(), timeBlockId, selectedBlockId, userPrincipal);

			// then - 삭제 성공, TimeBlock은 유지됨 (Block이 1개 남음)
			assertThat(timeBlockRepository.findByExternalId(plan.getId(), timeBlockId)).isPresent();

			List<Block> remainingBlocks = findBlocksByTimeBlockExternalId(timeBlockId);
			assertThat(remainingBlocks).hasSize(1);

			// 삭제된 Block이 목록에 없는지 확인
			boolean deletedBlockExists = remainingBlocks.stream()
				.anyMatch(block -> block.getExternalId().equals(selectedBlockId));
			assertThat(deletedBlockExists).isFalse();
		}
	}
}
