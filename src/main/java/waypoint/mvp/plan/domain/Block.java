package waypoint.mvp.plan.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.ExternalIdEntity;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.SocialMedia;

@Entity
@Table(name = "blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Block extends ExternalIdEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Place place;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private SocialMedia socialMedia;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private PlanMember addedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private TimeBlock timeBlock;

	@Column(length = 2000)
	private String memo;

	@Column(nullable = false)
	private boolean selected;

	@Builder(access = AccessLevel.PRIVATE)
	private Block(Place place, SocialMedia socialMedia, TimeBlock timeBlock, String memo, PlanMember addedBy) {
		this.place = place;
		this.socialMedia = socialMedia;
		this.timeBlock = timeBlock;
		this.memo = memo;
		this.selected = false;
		this.addedBy = addedBy;
	}

	public static Block create(Place place, SocialMedia socialMedia, TimeBlock timeBlock, String memo,
		PlanMember addedBy) {
		return builder()
			.place(place)
			.socialMedia(socialMedia)
			.timeBlock(timeBlock)
			.memo(memo)
			.addedBy(addedBy)
			.build();
	}

	public void select() {
		this.selected = true;
	}

	public void updateMemo(String memo) {
		this.memo = memo;
	}
}
