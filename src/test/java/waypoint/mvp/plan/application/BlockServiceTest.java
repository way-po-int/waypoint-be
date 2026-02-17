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
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.PlaceDetail;
import waypoint.mvp.place.infrastructure.persistence.PlaceRepository;
import waypoint.mvp.plan.application.dto.request.BlockCreateRequest;
import waypoint.mvp.plan.application.dto.request.CandidateBlockCreateRequest;
import waypoint.mvp.plan.application.dto.response.BlockResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.BlockStatus;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanCollection;
import waypoint.mvp.plan.domain.PlanDay;
import waypoint.mvp.plan.domain.PlanMember;
import waypoint.mvp.plan.domain.PlanRole;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.domain.TimeBlockType;
import waypoint.mvp.plan.infrastructure.persistence.BlockRepository;
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
	}

	// -- Fixture Helpers --

	private CollectionPlace createCollectionPlace(String placeName) {
		Collection collection = collectionRepository.save(Collection.create("Test Collection"));
		CollectionMember collectionMember = collectionMemberRepository.save(
			CollectionMember.create(collection, user, CollectionRole.OWNER));

		Place place = placeRepository.save(
			Place.create(placeName, "서울시 강남구",
				GEOMETRY_FACTORY.createPoint(new Coordinate(127.0, 37.5)),
				PlaceDetail.create("google-place-id-" + System.nanoTime()), 1L));

		CollectionPlace collectionPlace = collectionPlaceRepository.save(
			CollectionPlace.create(collection, place, collectionMember));

		planCollectionRepository.save(PlanCollection.create(plan, collection, planMember));

		return collectionPlace;
	}

	// -- Request Builders -----------

	private BlockCreateRequest placeBlockRequest(String collectionPlaceId, String memo) {
		return new BlockCreateRequest(
			collectionPlaceId, TimeBlockType.PLACE, DEFAULT_DAY, DEFAULT_START, DEFAULT_END, memo);
	}

	private BlockCreateRequest freeBlockRequest(String memo) {
		return new BlockCreateRequest(
			null, TimeBlockType.FREE, DEFAULT_DAY, DEFAULT_START, DEFAULT_END, memo);
	}

	// -- Service Call Helpers -----------

	private BlockResponse createBlock(BlockCreateRequest request) {
		return blockService.createBlock(plan.getExternalId(), request, userPrincipal);
	}

	private BlockResponse addCandidates(String timeBlockExternalId, List<String> collectionPlaceIds) {
		CandidateBlockCreateRequest request = new CandidateBlockCreateRequest(collectionPlaceIds);
		return blockService.addCandidates(
			plan.getExternalId(), timeBlockExternalId, request, userPrincipal);
	}

	// -- DB Helpers --

	private List<Block> findBlocksByTimeBlockExternalId(String timeBlockExternalId) {
		Long timeBlockId = timeBlockRepository.findByPlanId(plan.getId(), timeBlockExternalId)
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
			assertThatBlock(response)
				.hasStatus(BlockStatus.DIRECT)
				.hasType(TimeBlockType.PLACE)
				.hasCandidateCount(1)
				.hasStartTime(DEFAULT_START)
				.hasEndTime(DEFAULT_END)
				.hasSelectedBlockWithMemo("점심 예약 필요")
				.hasSelectedBlockWithPlaceName("강남 맛집")
				.hasSelectedBlockAddedBy("tester");

			assertThatCandidate(response.selectedBlock())
				.isSelected();

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
			assertThatBlock(response)
				.hasStatus(BlockStatus.NOTHING)
				.hasType(TimeBlockType.FREE)
				.hasCandidateCount(1)
				.hasStartTime(DEFAULT_START)
				.hasEndTime(DEFAULT_END);

			assertThat(response.candidates()).hasSize(1);
			assertThatCandidate(response.candidates().get(0))
				.hasMemo("카페에서 휴식")
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
			BlockResponse response = addCandidates(
				createResponse.timeBlockId(), List.of(cp2.getExternalId()));

			// then
			assertThatBlock(response)
				.hasStatus(BlockStatus.PENDING)
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
			BlockResponse pendingResponse = addCandidates(
				createResponse.timeBlockId(), List.of(cp2.getExternalId()));
			assertThat(pendingResponse.blockStatus()).isEqualTo(BlockStatus.PENDING);

			// when
			List<Block> allBlocks = findBlocksByTimeBlockExternalId(pendingResponse.timeBlockId());
			Block firstBlock = allBlocks.get(0);
			firstBlock.select();
			blockRepository.flush();

			// then
			Block selectedBlock = allBlocks.stream()
				.filter(Block::isSelected)
				.findFirst()
				.orElse(null);
			BlockStatus status = TimeBlock.determine(TimeBlockType.PLACE, selectedBlock, allBlocks);

			assertThat(selectedBlock).isNotNull();
			assertThat(status).isEqualTo(BlockStatus.FIXED);
		}
	}
}
