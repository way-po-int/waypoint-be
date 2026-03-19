package waypoint.mvp.global.auth.application;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.global.auth.application.dto.MemberCache;
import waypoint.mvp.global.config.CacheConfig;
import waypoint.mvp.plan.domain.PlanMember;
import waypoint.mvp.plan.infrastructure.persistence.PlanMemberRepository;

@Service
@RequiredArgsConstructor
public class MemberCacheService {

	private final CollectionMemberRepository collectionMemberRepository;
	private final PlanMemberRepository planMemberRepository;

	@Cacheable(value = CacheConfig.COLLECTION_MEMBERS_CACHE, key = "#collectionId")
	public MemberCache getCollectionMemberCache(Long collectionId) {
		List<CollectionMember> members = collectionMemberRepository.findActiveAll(collectionId);

		Set<Long> memberUserIds = new HashSet<>();
		Long ownerId = null;

		for (CollectionMember member : members) {
			Long userId = member.getUser().getId();
			memberUserIds.add(userId);

			if (member.isOwner()) {
				ownerId = userId;
			}
		}

		return new MemberCache(memberUserIds, ownerId);
	}

	@Cacheable(value = CacheConfig.PLAN_MEMBERS_CACHE, key = "#planId")
	public MemberCache getPlanMemberCache(Long planId) {
		List<PlanMember> members = planMemberRepository.findActiveAll(planId);

		Set<Long> memberUserIds = new HashSet<>();
		Long ownerId = null;

		for (PlanMember member : members) {
			Long userId = member.getUser().getId();
			memberUserIds.add(userId);

			if (member.isOwner()) {
				ownerId = userId;
			}
		}

		return new MemberCache(memberUserIds, ownerId);
	}

	@CacheEvict(value = CacheConfig.COLLECTION_MEMBERS_CACHE, key = "#collectionId")
	public void evictCollectionMembersCache(Long collectionId) {
		// Empty method body - @CacheEvict 사용을 위해서
	}

	@CacheEvict(value = CacheConfig.PLAN_MEMBERS_CACHE, key = "#planId")
	public void evictPlanMembersCache(Long planId) {
		// Empty method body - @CacheEvict 사용을 위해서
	}
}
