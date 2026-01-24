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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.BaseTimeEntity;
import waypoint.mvp.user.domain.User;

@Entity
@Table(name = "collection_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionMember extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Collection collection;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private User user;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false)
	private String picture;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CollectionRole role;

	@Builder(access = AccessLevel.PRIVATE)
	private CollectionMember(Collection collection, User user, String nickname, String picture,
		CollectionRole role) {
		this.collection = collection;
		this.user = user;
		this.nickname = nickname;
		this.picture = picture;
		this.role = role;
	}

	public static CollectionMember create(Collection collection, User user, CollectionRole role) {
		return builder()
			.collection(collection)
			.user(user)
			.nickname(user.getNickname())
			.picture(user.getPicture())
			.role(role)
			.build();
	}

	public boolean isOwner() {
		return this.role == CollectionRole.OWNER;
	}
}
