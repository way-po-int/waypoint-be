package waypoint.mvp.plan.application.dto.response;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import waypoint.mvp.collection.application.dto.response.SocialMediaResponse;
import waypoint.mvp.place.application.dto.PlaceResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.domain.TimeBlockType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BlockDetailResponse(
	String timeBlockId,
	TimeBlockType type,
	int day,// BlockResponse에 없음

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime startTime,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime endTime,

	String memo,
	PlaceResponse place,
	SocialMediaResponse socialMedia

	// TODO 의견 API 작업시 Opinion 추가
) {

	public static BlockDetailResponse from(Block block, PlaceResponse place) {
		TimeBlock timeBlock = block.getTimeBlock();

		return new BlockDetailResponse(
			timeBlock.getExternalId(),
			timeBlock.getType(),
			timeBlock.getPlanDay().getDay(),
			timeBlock.getStartTime(),
			timeBlock.getEndTime(),
			block.getMemo(),
			place,
			SocialMediaResponse.from(block.getSocialMedia())
		);
	}
}
