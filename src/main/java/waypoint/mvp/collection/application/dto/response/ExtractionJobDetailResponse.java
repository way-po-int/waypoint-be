package waypoint.mvp.collection.application.dto.response;

import static waypoint.mvp.collection.domain.PlaceExtractionJob.*;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import waypoint.mvp.collection.domain.PlaceExtractionJob;
import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.domain.SocialMediaPlace;
import waypoint.mvp.place.domain.SocialMediaStatus;
import waypoint.mvp.place.domain.SocialMediaType;
import waypoint.mvp.place.error.ExtractFailureCode;

public record ExtractionJobDetailResponse(
	String jobId,
	Instant requestedAt,
	SocialMediaStatus status,
	ExtractFailureCode failureCode,
	String failureMessage,
	DecisionStatus decisionStatus,
	Instant decidedAt,
	Result result
) {
	public static ExtractionJobDetailResponse of(
		PlaceExtractionJob job,
		List<SocialMediaPlace> socialMediaPlaces
	) {
		SocialMedia socialMedia = job.getSocialMedia();

		Result result = null;
		if (socialMedia.getStatus() == SocialMediaStatus.COMPLETED) {
			result = Result.of(socialMedia, socialMediaPlaces);
		}

		String failureMessage = socialMedia.getFailureCode() != null
			? socialMedia.getFailureCode().getMessage()
			: null;

		return new ExtractionJobDetailResponse(
			job.getExternalId(),
			job.getCreatedAt(),
			socialMedia.getStatus(),
			socialMedia.getFailureCode(),
			failureMessage,
			job.getDecisionStatus(),
			job.getDecidedAt(),
			result
		);
	}

	public record Result(
		int detectedCount,
		int matchedCount,
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
				.filter(SocialMediaPlace::isCompleted)
				.map(Place::from)
				.filter(Objects::nonNull)
				.toList();

			return new Result(
				socialMediaPlaces.size(),
				places.size(),
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
