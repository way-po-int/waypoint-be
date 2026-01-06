package waypoint.mvp.place.domain;

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
import waypoint.mvp.collection.domain.ExtractStatus;
import waypoint.mvp.global.common.BaseTimeEntity;

@Entity
@Table(name = "social_media")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialMedia extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SocialMediaType type;

	@Column(nullable = false)
	private String url;

	@Column
	private String title;

	@Column
	private String summary;

	@Enumerated(EnumType.STRING)
	private ExtractStatus status;

	@Builder(access = AccessLevel.PRIVATE)
	private SocialMedia(SocialMediaType type, String url) {
		this.type = type;
		this.url = url;
		this.status = ExtractStatus.PENDING;
	}

	public static SocialMedia create(SocialMediaType type, String url) {
		return builder()
			.type(type)
			.url(url)
			.build();
	}

	public void completeAnalysis(String title, String summary) {
		this.title = title;
		this.summary = summary;
		this.status = ExtractStatus.COMPLETED;
	}
}
