package waypoint.mvp.plan.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.application.CollectionService;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.plan.application.dto.request.CreatePlanCollectionRequest;
import waypoint.mvp.plan.application.dto.response.PlanCollectionResponse;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanCollection;
import waypoint.mvp.plan.domain.PlanMember;
import waypoint.mvp.plan.infrastructure.persistence.PlanCollectionRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanCollectionService {
	private final CollectionService collectionService;
	private final PlanMemberService planMemberService;
	private final PlanService planService;
	private final PlanCollectionRepository planCollectionRepository;
	private final ResourceAuthorizer planAuthorizer;
	private final ResourceAuthorizer collectionAuthorizer;

	@Transactional
	public PlanCollectionResponse createPlanCollection(
		String planExternalId,
		CreatePlanCollectionRequest request,
		UserPrincipal user
	) {
		Collection collection = collectionService.getEntity(planExternalId);
		Plan plan = planService.getEntity(request.collectionId());

		planAuthorizer.verifyMember(user, plan.getId());
		collectionAuthorizer.verifyMember(user, collection.getId());

		PlanMember member = planMemberService.getMemberByUserId(plan.getId(), user.getId());
		PlanCollection planCollection = PlanCollection.create(plan, collection, member);
		PlanCollection savedPlanCollection = planCollectionRepository.save(planCollection);

		return PlanCollectionResponse.from(savedPlanCollection);
	}

}
