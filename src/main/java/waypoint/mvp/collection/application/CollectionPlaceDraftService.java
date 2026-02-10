package waypoint.mvp.collection.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceDraftCreateRequest;
import waypoint.mvp.collection.application.dto.response.ExtractionJobResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionPlaceDraft;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceDraftRepository;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.place.application.SocialMediaService;
import waypoint.mvp.place.application.dto.SocialMediaInfo;
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

		// 장소 추출 이벤트 요청
		SocialMediaInfo socialMediaInfo = socialMediaService.addJob(request.url());

		// 어떤 멤버가 어떤 URL을 요청했는지 구분하기 위한 중간 테이블
		CollectionPlaceDraft draft = createOrGetDraft(member, socialMediaInfo.id());

		return new ExtractionJobResponse(
			draft.getExternalId(),
			socialMediaInfo.status()
		);
	}

	private CollectionPlaceDraft createOrGetDraft(CollectionMember member, Long socialMediaId) {
		return draftRepository.findByMemberIdAndSocialMediaId(member.getId(), socialMediaId)
			.orElseGet(() -> {
				SocialMedia media = socialMediaService.getSocialMedia(socialMediaId);
				CollectionPlaceDraft draft = CollectionPlaceDraft.create(member, media);
				return draftRepository.save(draft);
			});
	}
}
