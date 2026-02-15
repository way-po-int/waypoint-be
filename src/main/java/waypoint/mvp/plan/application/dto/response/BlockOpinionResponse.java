package waypoint.mvp.plan.application.dto.response;

import java.util.List;

import waypoint.mvp.plan.domain.BlockOpinion;
import waypoint.mvp.plan.domain.BlockOpinionType;

public record BlockOpinionResponse(
	String opinionId,
	BlockOpinionType type,
	String comment,
	List<String> opinionTagIds,
	PlanAddedBy addedBy
) {
	public static BlockOpinionResponse from(BlockOpinion opinion) {
		return new BlockOpinionResponse(
			opinion.getExternalId(),
			opinion.getType(),
			opinion.getComment(),
			opinion.getOpinionTagIds(),
			PlanAddedBy.from(opinion.getAddedBy())
		);
	}
}
