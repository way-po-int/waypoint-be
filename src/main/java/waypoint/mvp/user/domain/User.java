package waypoint.mvp.user.domain;

import java.time.Instant;

import org.hibernate.annotations.SQLRestriction;

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
import waypoint.mvp.global.common.LogicalDeleteEntity;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.user.error.UserError;

@Entity
@Table(
	name = "users",
	uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class User extends LogicalDeleteEntity {

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

	private Instant termsAcceptedAt;

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

	public boolean isTermsAccepted() {
		return termsAcceptedAt != null;
	}

	public void acceptTerms() {
		if (isTermsAccepted()) {
			throw new BusinessException(UserError.TERMS_ALREADY_ACCEPTED);
		}
		termsAcceptedAt = Instant.now();
	}

	public void changeNickname(String nickname) {
		this.nickname = nickname;
	}

	public void changePicture(String picture) {
		this.picture = picture;
	}

	public void delete() {
		super.softDelete();
	}
}
