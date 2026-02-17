package waypoint.mvp.plan.application.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanDay;

public record BlockListResponse(
	DayInfoResponse dayInfo,
	@JsonUnwrapped
	SliceResponse<BlockResponse> slice
) {
	public static BlockListResponse from(
		PlanDay planDay,
		Plan plan,
		Slice<?> slice,
		List<BlockResponse> contents
	) {
		return new BlockListResponse(
			DayInfoResponse.from(planDay, plan),
			SliceResponse.from(slice, contents)
		);
	}
}
