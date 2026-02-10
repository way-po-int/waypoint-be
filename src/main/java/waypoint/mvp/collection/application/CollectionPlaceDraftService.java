package waypoint.mvp.collection.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceDraftCreateRequest;
import waypoint.mvp.collection.application.dto.response.ExtractionJobResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionPlaceDraft;
import waypoint.mvp.collection.error.CollectionPlaceDraftError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceDraftRepository;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.SocialMediaService;
import waypoint.mvp.place.domain.SocialMedia;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionPlaceDraftService {

	private final CollectionService collectionService;
	private final CollectionMemberService collectionMemberService;
	private final SocialMediaService socialMediaService;
	private final CollectionPlaceDraftRepository draftRepository;
	private final ResourceAuthorizer collectionAuthorizer;

	@Transactional
	public ExtractionJobResponse createDraft(
		String collectionId,
		CollectionPlaceDraftCreateRequest request,
		AuthPrincipal user
	) {
		Collection collection = collectionService.getCollection(collectionId);
		collectionAuthorizer.verifyMember(user, collection.getId());

		CollectionMember member = collectionMemberService.findMemberByUserId(collection.getId(), user.getId());

		// 이미 진행 중인 작업이 있는지 확인
		Optional<CollectionPlaceDraft> existingDraft = draftRepository.findByMemberId(member.getId());
		if (existingDraft.isPresent()) {
			throw new BusinessException(CollectionPlaceDraftError.DRAFT_IN_PROGRESS)
				.addProperty("job_id", existingDraft.get().getExternalId());
		}

		// 장소 추출 이벤트 요청
		SocialMedia socialMedia = socialMediaService.getOrCreateSocialMedia(request.url());

		// 어떤 멤버가 어떤 URL을 요청했는지 구분하기 위한 중간 테이블
		CollectionPlaceDraft draft = CollectionPlaceDraft.create(member, socialMedia);
		draftRepository.save(draft);

		return new ExtractionJobResponse(
			draft.getExternalId(),
			socialMedia.getStatus()
		);
	}
}
