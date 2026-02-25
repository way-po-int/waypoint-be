package waypoint.mvp.plan.application.dto.response;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanDay;
import waypoint.mvp.plan.domain.TimeBlock;

public record CandidateGroupResponse(
	String timeBlockId,
	String title,
	DayInfoResponse dayInfo,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime startTime,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime endTime,

	Integer candidateCount,
	List<CandidateExpenseResponse> candidates
) {
	public static CandidateGroupResponse of(
		TimeBlock timeBlock,
		List<CandidateExpenseResponse> candidates
	) {
		PlanDay planDay = timeBlock.getPlanDay();
		Plan plan = planDay.getPlan();

		return new CandidateGroupResponse(
			timeBlock.getExternalId(),
			plan.getTitle(),
			DayInfoResponse.from(planDay, plan),
			timeBlock.getStartTime(),
			timeBlock.getEndTime(),
			candidates.size(),
			candidates
		);
	}
}
