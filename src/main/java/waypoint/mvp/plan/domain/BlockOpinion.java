package waypoint.mvp.plan.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import waypoint.mvp.global.common.ExternalIdEntity;

@Entity
@Table(name = "place_block_opinions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockOpinion extends ExternalIdEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Block block;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	PlanMember addedBy;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BlockOpinionType type;

	@Column(nullable = false)
	private String comment;

	@ElementCollection
	@CollectionTable(
		name = "place_block_opinion_tags",
		joinColumns = @JoinColumn(name = "block_opinions_id")
	)
	@Column(name = "opinion_tag_id")
	private List<String> opinionTagIds = new ArrayList<>();

	@Builder(access = AccessLevel.PRIVATE)
	private BlockOpinion(Block block, PlanMember addedBy, BlockOpinionType type, String comment,
		List<String> opinionTagIds) {
		this.block = block;
		this.addedBy = addedBy;
		this.type = type;
		this.comment = comment;
		this.opinionTagIds = (opinionTagIds != null) ? opinionTagIds : new ArrayList<>();
	}

	public static BlockOpinion create(Block block, PlanMember addedBy, BlockOpinionType type, String comment,
		List<String> opinionTagIds) {
		return builder()
			.block(block)
			.addedBy(addedBy)
			.type(type)
			.comment(comment)
			.opinionTagIds(opinionTagIds)
			.build();
	}
}
