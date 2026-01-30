package waypoint.mvp.plan.application;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.annotation.ServiceTest;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.application.dto.request.PlanCreateRequest;
import waypoint.mvp.plan.application.dto.response.PlanResponse;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanMember;
import waypoint.mvp.plan.domain.PlanRole;
import waypoint.mvp.plan.error.PlanError;
import waypoint.mvp.plan.infrastructure.persistence.PlanMemberRepository;
import waypoint.mvp.plan.infrastructure.persistence.PlanRepository;
import waypoint.mvp.user.domain.Provider;
import waypoint.mvp.user.domain.SocialAccount;
import waypoint.mvp.user.domain.User;
import waypoint.mvp.user.infrastructure.persistence.UserRepository;

@ServiceTest
class PlanServiceTest {

	@Autowired
	private PlanService planService;

	@Autowired
	private PlanRepository planRepository;

	@Autowired
	private PlanMemberRepository planMemberRepository;

	@Autowired
	private UserRepository userRepository;

	private User baseUser;
	private UserPrincipal requestPrincipal;

	@BeforeEach
	void setUp() {
		SocialAccount socialAccount = SocialAccount.create(Provider.GOOGLE, "12345");
		User user = User.create(socialAccount, "tester", "picture_url", "test@example.com");
		baseUser = userRepository.save(user);
		requestPrincipal = new UserPrincipal(baseUser.getId());
	}

	@Test
	@DisplayName("사용자는 성공적으로 Plan을 생성할 수 있다.")
	void createPlan_success() {
		// given
		String title = "First Plan";
		LocalDate startDate = LocalDate.of(2025, 3, 1);
		LocalDate endDate = LocalDate.of(2025, 3, 12);
		PlanCreateRequest createRequest = new PlanCreateRequest(title, startDate, endDate);

		// when
		PlanResponse response = planService.createPlan(createRequest, requestPrincipal);

		// then
		// 1. 응답 값 검증
		PlanResponse expectedResponse = new PlanResponse(null, title, startDate, endDate, 1);
		assertPlanResponseMatches(response, expectedResponse);

		// 2. DB 데이터 검증 (Side Effect 검증)
		Optional<Plan> foundPlanOpt = planRepository.findActiveByExternalId(response.planId());
		assertThat(foundPlanOpt).isPresent();
		Plan foundPlan = foundPlanOpt.get();

		Plan expectedPlan = Plan.create(title, startDate, endDate);
		assertPlanMatches(foundPlan, expectedPlan);

		Optional<PlanMember> foundMemberOpt = planMemberRepository.findActiveByUserId(foundPlan.getId(),
			baseUser.getId());
		assertThat(foundMemberOpt).isPresent();
		PlanMember foundMember = foundMemberOpt.get();
		assertThat(foundMember.getRole()).isEqualTo(PlanRole.OWNER);
	}

	@Test
	@DisplayName("Plan 생성 시, 종료일이 시작일보다 빠르면 예외가 발생한다.")
	void createPlan_fail_invalidDateRange() {
		// given
		String title = "Invalid Plan";
		LocalDate startDate = LocalDate.of(2024, 8, 10);
		LocalDate endDate = LocalDate.of(2024, 8, 1);
		PlanCreateRequest createRequest = new PlanCreateRequest(title, startDate, endDate);

		// when & then
		assertThatThrownBy(() -> planService.createPlan(createRequest, requestPrincipal))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getBody().getProperties().get("code"))
			.isEqualTo(PlanError.INVALID_DATE_RANGE.name());
	}

	private void assertPlanMatches(Plan actual, Plan expected) {
		assertThat(actual)
			.usingRecursiveComparison()
			.ignoringFields("id", "externalId", "createdAt", "updatedAt", "deletedAt")
			.isEqualTo(expected);
	}

	private void assertPlanResponseMatches(PlanResponse actual, PlanResponse expected) {
		assertThat(actual.planId()).isNotNull();

		assertThat(actual)
			.usingRecursiveComparison()
			.ignoringFields("planId")
			.isEqualTo(expected);
	}
}
