package waypoint.mvp.plan.application.dto.response;

import java.util.List;

import waypoint.mvp.place.application.dto.PlaceResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.BlockOpinion;

public record CandidateBlockResponse(
	String blockId,
	String memo,
	PlaceResponse place,
	boolean selected,
	PlanAddedBy addedBy,
	OpinionSummary opinionSummary,
	List<BlockOpinionResponse> opinions
) {
	public static CandidateBlockResponse from(Block block, PlaceResponse place, List<BlockOpinion> opinions) {
		return new CandidateBlockResponse(
			block.getExternalId(),
			block.getMemo(),
			place,
			block.isSelected(),
			PlanAddedBy.from(block.getAddedBy()),
			OpinionSummary.from(opinions),
			opinions.stream().map(BlockOpinionResponse::from).toList()
		);
	}
}
