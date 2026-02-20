package waypoint.mvp.plan.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.ExternalIdEntity;
import waypoint.mvp.place.domain.ManualPlace;
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
	@JoinColumn
	private Place place;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn
	private ManualPlace manualPlace;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
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

	@AssertTrue(message = "Place와 ManualPlace는 동시에 설정될 수 없습니다.")
	private boolean isValidPlaceSelection() {
		if (place != null && manualPlace != null) {
			return false; // 둘 다 있으면 Invalid
		}
		return true; // 둘 중 하나만 있거나, 둘 다 없거나(Null 가능)
	}

	@Builder(access = AccessLevel.PRIVATE)
	private Block(Place place, ManualPlace manualPlace, SocialMedia socialMedia, TimeBlock timeBlock, String memo,
		PlanMember addedBy) {
		this.place = place;
		this.manualPlace = manualPlace;
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

	public static Block createManual(ManualPlace manualPlace, TimeBlock timeBlock, String memo, PlanMember addedBy) {
		return builder()
			.manualPlace(manualPlace)
			.timeBlock(timeBlock)
			.memo(memo)
			.addedBy(addedBy)
			.build();
	}

	public static Block createFree(TimeBlock timeBlock, String memo, PlanMember addedBy) {
		return builder()
			.timeBlock(timeBlock)
			.memo(memo)
			.addedBy(addedBy)
			.build();
	}

	public void select() {
		this.selected = true;
	}

	public void unselect() {
		this.selected = false;
	}

	public void updateMemo(String memo) {
		this.memo = memo;
	}
}
