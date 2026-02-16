package waypoint.mvp.plan.application.dto;

import java.util.List;
import java.util.function.Function;

import waypoint.mvp.place.application.dto.PlaceResponse;
import waypoint.mvp.plan.application.dto.response.CandidateBlockResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.BlockStatus;
import waypoint.mvp.plan.domain.TimeBlock;

public record BlockAssembly(
	BlockStatus status,
	List<CandidateBlockResponse> candidates,
	CandidateBlockResponse selectedBlock
) {
	public static BlockAssembly of(List<Block> blocks, BlockOpinionDto blockOpinionDto,
		Function<Block, PlaceResponse> placeMapper, Long userId) {
		Block selectedBlock = blocks.stream().filter(Block::isSelected).findFirst().orElse(null);
		BlockStatus status = TimeBlock.determine(selectedBlock, blocks);

		List<CandidateBlockResponse> candidates = blocks.stream()
			.map(b -> CandidateBlockResponse.from(b, placeMapper.apply(b), blockOpinionDto.getOpinions(b.getId()),
				userId))
			.toList();

		CandidateBlockResponse selected = candidates.stream()
			.filter(CandidateBlockResponse::selected)
			.findFirst()
			.orElse(null);

		return new BlockAssembly(status, candidates, selected);
	}
}
