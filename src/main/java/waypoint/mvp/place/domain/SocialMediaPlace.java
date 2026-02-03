package waypoint.mvp.place.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.error.SocialMediaError;

@Entity
@Table(name = "social_media_places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialMediaPlace {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private Place place;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private SocialMedia socialMedia;

	@Column(nullable = false)
	private String searchQuery;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PlaceSearchStatus status;

	@Builder(access = AccessLevel.PRIVATE)
	private SocialMediaPlace(SocialMedia socialMedia, String searchQuery) {
		this.socialMedia = socialMedia;
		this.searchQuery = searchQuery;
		this.status = PlaceSearchStatus.PENDING;
	}

	public void process() {
		validateStatus(PlaceSearchStatus.PENDING);

		this.status = PlaceSearchStatus.PROCESSING;
	}

	public void complete(Place place) {
		validateStatus(PlaceSearchStatus.PROCESSING);

		this.place = place;
		this.status = PlaceSearchStatus.COMPLETED;
	}

	public void notFound() {
		validateStatus(PlaceSearchStatus.PROCESSING);

		this.status = PlaceSearchStatus.NOT_FOUND;
	}

	public void fail() {
		validateStatus(PlaceSearchStatus.PROCESSING);

		this.status = PlaceSearchStatus.FAILED;
	}

	public static SocialMediaPlace create(SocialMedia socialMedia, String searchQuery) {
		return builder()
			.socialMedia(socialMedia)
			.searchQuery(searchQuery)
			.build();
	}

	private void validateStatus(PlaceSearchStatus status) {
		if (this.status != status) {
			throw new BusinessException(SocialMediaError.SOCIAL_MEDIA_INVALID_STATUS, this.status, status);
		}
	}
}
