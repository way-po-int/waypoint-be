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
import waypoint.mvp.global.common.BaseTimeEntity;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.SocialMedia;

@Entity
@Table(name = "collection_places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionPlace extends BaseTimeEntity {

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
	@JoinColumn
	private SocialMedia socialMedia;

	@Column
	private String memo;

	@Builder(access = AccessLevel.PRIVATE)
	private CollectionPlace(Collection collection, Place place, SocialMedia socialMedia) {
		this.collection = collection;
		this.place = place;
		this.socialMedia = socialMedia;
	}

	public static CollectionPlace create(Collection collection, Place place, SocialMedia socialMedia) {
		return builder()
			.collection(collection)
			.place(place)
			.socialMedia(socialMedia)
			.build();
	}

	public static CollectionPlace create(Collection collection, Place place) {
		return builder()
			.collection(collection)
			.place(place)
			.build();
	}
}
