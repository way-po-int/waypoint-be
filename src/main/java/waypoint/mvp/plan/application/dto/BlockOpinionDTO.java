package waypoint.mvp.plan.application.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import waypoint.mvp.plan.domain.BlockOpinion;
import waypoint.mvp.plan.infrastructure.persistence.BlockOpinionRepository;

public record BlockOpinionDTO(
	Map<Long, List<BlockOpinion>> opinionsByBlockId
) {
	public List<BlockOpinion> getOpinions(Long blockId) {
		return opinionsByBlockId.getOrDefault(blockId, List.of());
	}

	public static BlockOpinionDTO from(List<Long> blockIds, BlockOpinionRepository blockOpinionRepository) {
		if (blockIds.isEmpty()) {
			return new BlockOpinionDTO(Collections.emptyMap());
		}
		Map<Long, List<BlockOpinion>> opinionsByBlockId = blockOpinionRepository.findAllByBlockIds(blockIds).stream()
			.collect(Collectors.groupingBy(opinion -> opinion.getBlock().getId()));
		return new BlockOpinionDTO(opinionsByBlockId);
	}
}
