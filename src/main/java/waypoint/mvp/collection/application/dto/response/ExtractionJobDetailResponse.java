package waypoint.mvp.collection.application.dto.response;

import java.util.List;
import java.util.Objects;

import waypoint.mvp.collection.domain.CollectionPlaceDraft;
import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.domain.SocialMediaPlace;
import waypoint.mvp.place.domain.SocialMediaStatus;
import waypoint.mvp.place.domain.SocialMediaType;
import waypoint.mvp.place.error.ExtractFailureCode;

public record ExtractionJobDetailResponse(
	String jobId,
	SocialMediaStatus status,
	ExtractFailureCode failureCode,
	String failureMessage,
	Result result
) {
	public static ExtractionJobDetailResponse of(
		CollectionPlaceDraft draft,
		List<SocialMediaPlace> socialMediaPlaces
	) {
		SocialMedia socialMedia = draft.getSocialMedia();

		Result result = null;
		if (socialMedia.getStatus() == SocialMediaStatus.COMPLETED) {
			result = Result.of(socialMedia, socialMediaPlaces);
		}

		String failureMessage = socialMedia.getFailureCode() != null
			? socialMedia.getFailureCode().getMessage()
			: null;

		return new ExtractionJobDetailResponse(
			draft.getExternalId(),
			socialMedia.getStatus(),
			socialMedia.getFailureCode(),
			failureMessage,
			result
		);
	}

	public record Result(
		SocialMediaType mediaType,
		String url,
		String authorName,
		String title,
		String summary,
		List<Place> places
	) {
		public static Result of(SocialMedia socialMedia, List<SocialMediaPlace> socialMediaPlaces) {
			var snapshot = socialMedia.getSnapshot();
			String title = snapshot != null ? snapshot.getTitle() : null;
			String authorName = snapshot != null ? snapshot.getAuthorName() : null;

			List<Place> places = socialMediaPlaces.stream()
				.map(Place::from)
				.filter(Objects::nonNull)
				.toList();

			return new Result(
				socialMedia.getType(),
				socialMedia.getUrl(),
				authorName,
				title,
				socialMedia.getSummary(),
				places
			);
		}

		public record Place(String placeId, String name, String address) {
			public static Place from(SocialMediaPlace socialMediaPlace) {
				var place = socialMediaPlace.getPlace();
				if (place == null) {
					return null;
				}
				return new Place(
					place.getExternalId(),
					place.getName(),
					place.getAddress()
				);
			}
		}
	}
}
