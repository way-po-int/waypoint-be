package waypoint.mvp.plan.application;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.application.CollectionPlaceQueryService;
import waypoint.mvp.collection.application.CollectionService;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceDetailResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionPlace;
import waypoint.mvp.collection.domain.PlaceSortType;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.application.dto.request.CreatePlanCollectionRequest;
import waypoint.mvp.plan.application.dto.response.PlanCollectionResponse;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanCollection;
import waypoint.mvp.plan.domain.PlanMember;
import waypoint.mvp.plan.error.PlanCollectionError;
import waypoint.mvp.plan.infrastructure.persistence.PlanCollectionRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanCollectionService {
	private final CollectionService collectionService;
	private final CollectionPlaceQueryService collectionPlaceQueryService;
	private final PlanMemberService planMemberService;
	private final PlanService planService;
	private final PlanCollectionRepository planCollectionRepository;
	private final ResourceAuthorizer planAuthorizer;
	private final ResourceAuthorizer collectionAuthorizer;

	/**
	 * plan에 collection추가는 collection의 멤버만 추가할 수 있다.
	 * */
	@Transactional
	public PlanCollectionResponse createPlanCollection(
		String planExternalId,
		CreatePlanCollectionRequest request,
		UserPrincipal user
	) {

		if (planCollectionRepository.existsByPlanIdAndCollectionId(planExternalId, request.collectionId())) {
			throw new BusinessException(PlanCollectionError.PLAN_COLLECTION_ALREADY_EXISTS);
		}

		Plan plan = planService.getPlan(planExternalId);
		Collection collection = collectionService.getCollection(request.collectionId());

		planAuthorizer.verifyMember(user, plan.getId());
		collectionAuthorizer.verifyMember(user, collection.getId());

		PlanMember member = planMemberService.findMemberByUserId(plan.getId(), user.getId());
		PlanCollection planCollection = PlanCollection.create(plan, collection, member);

		return PlanCollectionResponse.from(planCollectionRepository.save(planCollection));
	}

	public PlanCollection getPlanCollection(String planId, String collectionId) {
		return planCollectionRepository
			.findByPlanIdAndCollectionId(planId, collectionId)
			.orElseThrow(() -> new BusinessException(PlanCollectionError.PLAN_COLLECTION_NOT_FOUND));
	}

	public List<PlanCollectionResponse> findPlanCollectionResponses(String planId, AuthPrincipal user) {
		Plan plan = planService.getPlan(planId);
		planAuthorizer.verifyAccess(user, plan.getId());

		return planCollectionRepository.findAllByPlanId(planId)
			.stream().map(PlanCollectionResponse::from).toList();
	}

	public SliceResponse<CollectionPlaceResponse> findPlanCollectionPlaces(
		String planId,
		String collectionId,
		PlaceSortType sortType,
		Pageable pageable,
		AuthPrincipal user
	) {
		Plan plan = planService.getPlan(planId);
		planAuthorizer.verifyMember(user, plan.getId());

		PlanCollection planCollection = getPlanCollection(planId, collectionId);

		return collectionPlaceQueryService.getPlacesByCollectionId(
			planCollection.getCollection().getId(), null, sortType, pageable
		);
	}

	public CollectionPlaceDetailResponse findPlanCollectionPlaceDetail(
		String planId,
		String collectionId,
		String collectionPlaceId,
		UserPrincipal user
	) {
		Plan plan = planService.getPlan(planId);
		planAuthorizer.verifyMember(user, plan.getId());

		getPlanCollection(planId, collectionId);

		return collectionPlaceQueryService.getPlaceDetail(
			collectionId, collectionPlaceId
		);
	}

	// TODO 검증로직을 Service에 둘지 의논 필요(우선 순위 낮음)
	public void verifyPlacesLinkedToPlan(Long planId, List<CollectionPlace> collectionPlaces) {
		Set<Long> linkedCollectionIds = new HashSet<>(
			planCollectionRepository.findCollectionIdsByPlanId(planId)
		);

		boolean hasInvalidPlace = collectionPlaces.stream()
			.anyMatch(cp -> !linkedCollectionIds.contains(cp.getCollection().getId()));

		if (hasInvalidPlace) {
			throw new BusinessException(PlanCollectionError.PLAN_COLLECTION_NOT_FOUND);
		}
	}

	@Transactional
	public void deletePlanCollection(String planId, String collectionId, UserPrincipal user) {
		Plan plan = planService.getPlan(planId);
		planAuthorizer.verifyMember(user, plan.getId());

		PlanCollection planCollection = getPlanCollection(planId, collectionId);

		planCollectionRepository.delete(planCollection);
	}

}
