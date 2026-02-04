package waypoint.mvp.collection.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import waypoint.mvp.global.common.ExternalIdEntity;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.SocialMedia;

@Entity
@Table(name = "collection_places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionPlace extends ExternalIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Collection collection;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Place place;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "added_by_member_id", nullable = false)
	private CollectionMember addedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private SocialMedia socialMedia;

	@Column(length = 300)
	private String memo;

	@Builder(access = AccessLevel.PRIVATE)
	private CollectionPlace(Collection collection, Place place, CollectionMember addedBy, SocialMedia socialMedia) {
		this.collection = collection;
		this.place = place;
		this.addedBy = addedBy;
		this.socialMedia = socialMedia;
	}

	public static CollectionPlace create(Collection collection, Place place, CollectionMember addedBy) {
		return create(collection, place, addedBy, null);
	}

	public static CollectionPlace create(Collection collection, Place place, CollectionMember addedBy,
		SocialMedia socialMedia) {
		return builder()
			.collection(collection)
			.place(place)
			.addedBy(addedBy)
			.socialMedia(socialMedia)
			.build();
	}

	public void updateMemo(String memo) {
		this.memo = memo;
	}
}
