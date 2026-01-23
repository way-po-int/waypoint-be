package waypoint.mvp.collection.domain;

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
import waypoint.mvp.global.common.BaseTimeEntity;
import waypoint.mvp.place.domain.SocialMedia;

@Entity
@Table(
	name = "place_extraction_jobs",
	uniqueConstraints = @UniqueConstraint(columnNames = {"collection_member_id", "social_media_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceExtractionJob extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collection_member_id", nullable = false)
	private CollectionMember member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private SocialMedia socialMedia;

	@Builder(access = AccessLevel.PRIVATE)
	private PlaceExtractionJob(CollectionMember member, SocialMedia socialMedia) {
		this.member = member;
		this.socialMedia = socialMedia;
	}

	public static PlaceExtractionJob create(CollectionMember member, SocialMedia socialMedia) {
		return builder()
			.member(member)
			.socialMedia(socialMedia)
			.build();
	}
}
