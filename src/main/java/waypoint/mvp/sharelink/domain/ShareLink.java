package waypoint.mvp.sharelink.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.common.BaseTimeEntity;

@Entity
@Table(name = "share_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareLink extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ShareLinkType targetType;

	@Column(nullable = false)
	private Long targetId;

	@Column(nullable = false)
	private Long hostUserId;

	@Column(nullable = false)
	private int useCount;

	@Column(nullable = false)
	private Instant expiresAt;

	@Builder(access = AccessLevel.PRIVATE)
	private ShareLink(String code, ShareLinkType targetType, Long targetId, Long hostUserId, Instant expiresAt) {
		this.code = code;
		this.targetType = targetType;
		this.targetId = targetId;
		this.hostUserId = hostUserId;
		this.useCount = 0;
		this.expiresAt = expiresAt;
	}

	public static ShareLink create(ShareLinkType targetType, Long targetId, Long hostUserId,
		long expirationHours) {
		String code = UUID.randomUUID().toString();
		Instant expiresAt = Instant.now().plus(expirationHours, ChronoUnit.HOURS);

		return builder()
			.code(code)
			.targetType(targetType)
			.targetId(targetId)
			.hostUserId(hostUserId)
			.expiresAt(expiresAt)
			.build();
	}

	@Getter
	@RequiredArgsConstructor
	public enum ShareLinkType {
		COLLECTION("/collections/"),
		PLAN("/plans/");

		private final String path;
	}

	public void increaseUseCount() {
		this.useCount++;
	}

	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}
}
