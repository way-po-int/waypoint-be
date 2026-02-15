package waypoint.mvp.plan.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.plan.application.dto.BlockOpinionDto;
import waypoint.mvp.plan.domain.BlockOpinion;
import waypoint.mvp.plan.infrastructure.persistence.BlockOpinionRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlockOpinionQueryService {

	private final BlockOpinionRepository blockOpinionRepository;

	public BlockOpinionDto findByBlockIds(List<Long> blockIds) {
		if (blockIds.isEmpty()) {
			return new BlockOpinionDto(Map.of());
		}
		Map<Long, List<BlockOpinion>> opinionsByBlockId = blockOpinionRepository.findAllByBlockIds(blockIds)
			.stream()
			.collect(Collectors.groupingBy(opinion -> opinion.getBlock().getId()));
		return new BlockOpinionDto(opinionsByBlockId);
	}
}
