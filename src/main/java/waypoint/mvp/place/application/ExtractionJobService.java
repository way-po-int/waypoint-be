package waypoint.mvp.place.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.place.application.dto.ExtractionJobInfo;
import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.infrastructure.persistence.SocialMediaRepository;

@Service
@RequiredArgsConstructor
public class ExtractionJobService {

	private final SocialMediaRepository socialMediaRepository;

	@Transactional
	public ExtractionJobInfo addJob(String url) {
		SocialMedia socialMedia = createOrGetSocialMedia(url);
		return ExtractionJobInfo.from(socialMedia);
	}

	private SocialMedia createOrGetSocialMedia(String url) {
		return socialMediaRepository.findByUrl(url)
			.orElseGet(() -> socialMediaRepository.save(SocialMedia.create(url)));
	}
}
