package waypoint.mvp.plan.application.dto.response;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import waypoint.mvp.collection.application.dto.response.SocialMediaResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.domain.TimeBlockType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BlockDetailResponse(
	String timeBlockId,
	TimeBlockType type,
	DayInfoResponse dayInfo,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime startTime,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime endTime,

	List<BlockOpinionResponse> opinions,
	CandidateBlockResponse block,
	SocialMediaResponse socialMedia
) {

	public static BlockDetailResponse from(
		Block block,
		Plan plan,
		List<BlockOpinionResponse> opinions,
		CandidateBlockResponse candidateBlock
	) {
		TimeBlock timeBlock = block.getTimeBlock();

		return new BlockDetailResponse(
			timeBlock.getExternalId(),
			timeBlock.getType(),
			DayInfoResponse.from(timeBlock.getPlanDay(), plan),
			timeBlock.getStartTime(),
			timeBlock.getEndTime(),
			opinions,
			candidateBlock,
			block.getSocialMedia() != null ? SocialMediaResponse.from(block.getSocialMedia()) : null
		);
	}
}
