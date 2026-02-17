package waypoint.mvp.plan.application.dto;

import java.util.List;
import java.util.Map;

import waypoint.mvp.plan.domain.BlockOpinion;

public record BlockOpinionDto(
	Map<Long, List<BlockOpinion>> opinionsByBlockId
) {
	public List<BlockOpinion> getOpinions(Long blockId) {
		return opinionsByBlockId.getOrDefault(blockId, List.of());
	}
}
