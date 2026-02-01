package waypoint.mvp.place.domain;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
import waypoint.mvp.global.common.ExternalIdEntity;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.domain.content.ContentSnapshot;
import waypoint.mvp.place.error.SocialMediaError;

@Entity
@Table(name = "social_media")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialMedia extends ExternalIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SocialMediaType type;

	@Column(nullable = false, unique = true)
	private String url;

	@Column
	private String summary;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column
	private List<String> searchQueries;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column
	private ContentSnapshot snapshot;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ExtractStatus status;

	@Enumerated(EnumType.STRING)
	@Column
	private ExtractFailureCode failureCode;

	@Builder(access = AccessLevel.PRIVATE)
	private SocialMedia(SocialMediaType type, String url) {
		this.type = type;
		this.url = url;
		this.status = ExtractStatus.PENDING;
	}

	public static SocialMedia create(String url) {
		return builder()
			.type(SocialMediaType.from(url))
			.url(url)
			.build();
	}

	public void process() {
		validateStatus(ExtractStatus.PENDING);

		this.status = ExtractStatus.PROCESSING;
	}

	public void complete(String summary, List<String> searchQueries, ContentSnapshot snapshot) {
		validateStatus(ExtractStatus.PROCESSING);

		this.summary = summary;
		this.searchQueries = searchQueries;
		this.snapshot = snapshot;
		this.status = ExtractStatus.COMPLETED;
	}

	public void fail(ExtractFailureCode failureCode) {
		validateStatus(ExtractStatus.PROCESSING);

		this.status = ExtractStatus.FAILED;
		this.failureCode = failureCode;
	}

	private void validateStatus(ExtractStatus status) {
		if (this.status != status) {
			throw new BusinessException(SocialMediaError.SOCIAL_MEDIA_INVALID_STATUS, this.status, status);
		}
	}
}
