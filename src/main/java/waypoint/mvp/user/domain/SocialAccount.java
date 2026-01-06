package waypoint.mvp.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Provider provider;

	@Column(nullable = false)
	private String providerId;

	@Builder(access = AccessLevel.PRIVATE)
	private SocialAccount(Provider provider, String providerId) {
		this.provider = provider;
		this.providerId = providerId;
	}

	public static SocialAccount create(Provider provider, String providerId) {
		return builder()
			.provider(provider)
			.providerId(providerId)
			.build();
	}
}
