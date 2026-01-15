package waypoint.mvp.auth.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(nullable = false)
	private Instant expiresAt;

	@Builder(access = AccessLevel.PRIVATE)
	private RefreshToken(Long userId, String token, Instant expiresAt) {
		this.userId = userId;
		this.token = token;
		this.expiresAt = expiresAt;
	}

	public static RefreshToken create(Long userId, String token, Instant expiresAt) {
		return builder()
			.userId(userId)
			.token(token)
			.expiresAt(expiresAt)
			.build();
	}

	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}
}
