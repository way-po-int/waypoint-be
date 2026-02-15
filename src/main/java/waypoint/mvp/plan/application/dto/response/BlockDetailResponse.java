package waypoint.mvp.plan.application.dto.response;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import waypoint.mvp.collection.application.dto.response.SocialMediaResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.BlockStatus;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.domain.TimeBlockType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BlockDetailResponse(
	String timeBlockId,
	TimeBlockType type,
	BlockStatus blockStatus,
	int day,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime startTime,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime endTime,

	Integer candidateCount,
	List<CandidateBlockResponse> candidates,
	CandidateBlockResponse selectedBlock,
	SocialMediaResponse socialMedia
) {

	public static BlockDetailResponse from(
		Block block,
		BlockStatus blockStatus,
		List<CandidateBlockResponse> candidates,
		CandidateBlockResponse selectedBlock
	) {
		TimeBlock timeBlock = block.getTimeBlock();

		return new BlockDetailResponse(
			timeBlock.getExternalId(),
			timeBlock.getType(),
			blockStatus,
			timeBlock.getPlanDay().getDay(),
			timeBlock.getStartTime(),
			timeBlock.getEndTime(),
			candidates != null ? candidates.size() : 0,
			candidates,
			selectedBlock,
			block.getSocialMedia() != null ? SocialMediaResponse.from(block.getSocialMedia()) : null
		);
	}
}
