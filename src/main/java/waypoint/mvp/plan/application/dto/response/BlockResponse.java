package waypoint.mvp.plan.application.dto.response;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import waypoint.mvp.plan.domain.BlockStatus;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.domain.TimeBlockType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BlockResponse(
	String timeBlockId,
	TimeBlockType type,
	BlockStatus blockStatus,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime startTime,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime endTime,

	Integer candidateCount,
	List<CandidateBlockResponse> candidates,
	CandidateBlockResponse selectedBlock
) {

	public static BlockResponse from(
		TimeBlock timeBlock,
		BlockStatus blockStatus,
		List<CandidateBlockResponse> candidates,
		CandidateBlockResponse selectedBlock
	) {
		return new BlockResponse(
			timeBlock.getExternalId(),
			timeBlock.getType(),
			blockStatus,
			timeBlock.getStartTime(),
			timeBlock.getEndTime(),
			candidates != null ? candidates.size() : 0,
			candidates,
			selectedBlock
		);
	}
}
