package waypoint.mvp.collection.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceFromUrlRequest;
import waypoint.mvp.collection.application.dto.response.ExtractionJobResponse;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionPlaceDraft;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceDraftRepository;
import waypoint.mvp.place.application.SocialMediaService;
import waypoint.mvp.place.application.dto.SocialMediaInfo;
import waypoint.mvp.place.domain.SocialMedia;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionPlaceService {

	private final SocialMediaService socialMediaService;
	private final CollectionMemberService collectionMemberService;
	private final CollectionPlaceDraftRepository jobRepository;

	@Transactional
	public ExtractionJobResponse addPlacesFromUrl(Long collectionId, CollectionPlaceFromUrlRequest request,
		AuthPrincipal user) {
		CollectionMember collectionMember = collectionMemberService.getMemberByUserId(collectionId, user.getId());

		// 장소 추출 이벤트 요청
		SocialMediaInfo socialMediaInfo = socialMediaService.addJob(request.url());

		// 어떤 멤버가 어떤 URL을 요청했는지 구분하기 위한 중간 테이블
		CollectionPlaceDraft draft = createOrGetDraft(collectionMember, socialMediaInfo.id());
		return new ExtractionJobResponse(
			draft.getId().toString(),
			socialMediaInfo.status()
		);
	}

	private CollectionPlaceDraft createOrGetDraft(CollectionMember member, Long socialMediaId) {
		return jobRepository.findByMemberIdAndSocialMediaId(member.getId(), socialMediaId)
			.orElseGet(() -> {
				SocialMedia media = socialMediaService.getSocialMedia(socialMediaId);

				var draft = CollectionPlaceDraft.create(member, media);
				return jobRepository.save(draft);
			});
	}
}
