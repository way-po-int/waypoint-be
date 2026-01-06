package waypoint.mvp.place.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "social_media_places",
	uniqueConstraints = @UniqueConstraint(columnNames = {"place_id", "social_media_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialMediaPlace {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Place place;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private SocialMedia socialMedia;

	@Builder(access = AccessLevel.PRIVATE)
	private SocialMediaPlace(Place place, SocialMedia socialMedia) {
		this.place = place;
		this.socialMedia = socialMedia;
	}

	public static SocialMediaPlace create(Place place, SocialMedia socialMedia) {
		return builder()
			.place(place)
			.socialMedia(socialMedia)
			.build();
	}
}
