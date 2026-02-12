package waypoint.mvp.collection.domain;

import java.time.Instant;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

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
import waypoint.mvp.collection.error.PlaceExtractionJobError;
import waypoint.mvp.global.common.BaseTimeEntity;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.domain.SocialMedia;

@Entity
@Table(name = "place_extraction_jobs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceExtractionJob extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, updatable = false, length = 21)
	private String jobId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collection_member_id", nullable = false)
	private CollectionMember member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private SocialMedia socialMedia;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private DecisionStatus decisionStatus;

	@Column
	private Instant decidedAt;

	@Builder(access = AccessLevel.PRIVATE)
	private PlaceExtractionJob(String jobId, CollectionMember member, SocialMedia socialMedia) {
		this.jobId = jobId;
		this.member = member;
		this.socialMedia = socialMedia;
		this.decisionStatus = DecisionStatus.UNDECIDED;
	}

	public static PlaceExtractionJob create(CollectionMember member, SocialMedia socialMedia) {
		String jobId = NanoIdUtils.randomNanoId();
		return builder()
			.jobId(jobId)
			.member(member)
			.socialMedia(socialMedia)
			.build();
	}

	public void select() {
		validateDecisionStatus();
		this.decisionStatus = DecisionStatus.SELECTED;
		this.decidedAt = Instant.now();
	}

	public void ignore() {
		validateDecisionStatus();
		this.decisionStatus = DecisionStatus.IGNORED;
		this.decidedAt = Instant.now();
	}

	private void validateDecisionStatus() {
		if (this.decisionStatus != DecisionStatus.UNDECIDED) {
			throw new BusinessException(PlaceExtractionJobError.ALREADY_DECIDED);
		}
	}

	public enum DecisionStatus {
		UNDECIDED,
		SELECTED,
		IGNORED
	}
}
