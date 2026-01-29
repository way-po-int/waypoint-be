package waypoint.mvp.collection.domain;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.common.BaseTimeEntity;
import waypoint.mvp.place.domain.SocialMedia;

@Entity
@Table(
	name = "collection_place_drafts",
	uniqueConstraints = @UniqueConstraint(columnNames = {"collection_member_id", "social_media_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionPlaceDraft extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collection_member_id", nullable = false)
	private CollectionMember member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private SocialMedia socialMedia;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DraftStatus status;

	@Builder(access = AccessLevel.PRIVATE)
	private CollectionPlaceDraft(CollectionMember member, SocialMedia socialMedia) {
		this.member = member;
		this.socialMedia = socialMedia;
		this.status = DraftStatus.WAITING;
	}

	public static CollectionPlaceDraft create(CollectionMember member, SocialMedia socialMedia) {
		return builder()
			.member(member)
			.socialMedia(socialMedia)
			.build();
	}

	@Getter
	@RequiredArgsConstructor
	public enum DraftStatus {
		WAITING,
		COMPLETED
	}
}
