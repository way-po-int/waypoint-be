package waypoint.mvp.collection.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
import waypoint.mvp.global.common.BaseTimeEntity;

@Entity
@Table(
	name = "collection_place_preferences",
	uniqueConstraints = @UniqueConstraint(columnNames = {"collection_place_id", "collection_member_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionPlacePreference extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "collection_place_id", nullable = false)
	private CollectionPlace place;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collection_member_id", nullable = false)
	private CollectionMember member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Type type;

	@Builder(access = AccessLevel.PRIVATE)
	private CollectionPlacePreference(CollectionPlace place, CollectionMember member, Type type) {
		this.place = place;
		this.member = member;
		this.type = type;
	}

	public static CollectionPlacePreference create(CollectionPlace place, CollectionMember member, Type type) {
		return builder()
			.place(place)
			.member(member)
			.type(type)
			.build();
	}

	public void changeType(Type type) {
		this.type = type;
	}

	public enum Type {
		PICK,
		PASS
	}
}
