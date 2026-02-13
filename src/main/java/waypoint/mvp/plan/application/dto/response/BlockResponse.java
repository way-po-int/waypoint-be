package waypoint.mvp.plan.application.dto.response;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import waypoint.mvp.place.application.dto.PlaceResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.domain.TimeBlockType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BlockResponse(
	String timeBlockId,
	TimeBlockType type,
	// TODO  후보지 API 작업시 blockStatus 필요

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime startTime,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime endTime,

	// TODO 후보지 API 작업시 candidate_count, candidates 추가

	SelectedBlock selectedBlock
) {

	/**
	 *  TimeBlockType [FREE, Place] 모두 1개에 메서드로 사용됨
	 */
	public static BlockResponse from(TimeBlock timeBlock, Block block, PlaceResponse placeResponse) {
		return new BlockResponse(
			timeBlock.getExternalId(),
			timeBlock.getType(),
			timeBlock.getStartTime(),
			timeBlock.getEndTime(),
			SelectedBlock.from(block, placeResponse)
		);
	}

	record SelectedBlock(
		String blockId,
		String memo,
		PlanAddedBy addedBy,
		PlaceResponse place
		// TODO 의견 API 작업시 Opinion 추가
	) {

		public static SelectedBlock from(Block block, PlaceResponse placeResponse) {
			return new SelectedBlock(
				block.getExternalId(),
				block.getMemo(),
				PlanAddedBy.from(block.getAddedBy()),
				placeResponse
			);
		}
	}
}
