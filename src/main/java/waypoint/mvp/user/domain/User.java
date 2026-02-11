package waypoint.mvp.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.ExternalIdEntity;

@Entity
@Table(
	name = "users",
	uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends ExternalIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private SocialAccount socialAccount;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false)
	private String picture;

	@Column(nullable = false)
	private String email;

	@Builder(access = AccessLevel.PRIVATE)
	private User(SocialAccount socialAccount, String nickname, String picture, String email) {
		this.socialAccount = socialAccount;
		this.nickname = nickname;
		this.picture = picture;
		this.email = email;
	}

	public static User create(SocialAccount socialAccount, String nickname, String picture, String email) {
		return builder()
			.socialAccount(socialAccount)
			.nickname(nickname)
			.picture(picture)
			.email(email)
			.build();
	}
}
