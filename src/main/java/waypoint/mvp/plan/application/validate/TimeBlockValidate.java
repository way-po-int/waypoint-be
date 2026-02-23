package waypoint.mvp.plan.application.validate;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.error.BlockError;
import waypoint.mvp.plan.infrastructure.persistence.TimeBlockRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimeBlockValidate {

	private final TimeBlockRepository timeBlockRepository;

	public void validateTimeOverlap(Long planDayId, LocalTime startTime, LocalTime endTime, Long excludeTimeBlockId) {
		List<TimeBlock> overlappingBlocks;

		if (excludeTimeBlockId != null) {
			overlappingBlocks = timeBlockRepository.findOverlappingTimeBlocksExcludingId(
				planDayId, startTime, endTime, excludeTimeBlockId);
		} else {
			overlappingBlocks = timeBlockRepository.findOverlappingTimeBlocks(
				planDayId, startTime, endTime);
		}

		if (!overlappingBlocks.isEmpty()) {
			for (TimeBlock existing : overlappingBlocks) {
				if (existing.getStartTime().equals(startTime) && existing.getEndTime().equals(endTime)) {
					throw new BusinessException(BlockError.TIME_BLOCK_EXACT_DUPLICATE)
						.addProperty("timeBlockId", existing.getExternalId());
				}
			}
			throw new BusinessException(BlockError.TIME_BLOCK_OVERLAP);
		}
	}
}
