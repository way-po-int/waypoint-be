package waypoint.mvp.plan.application.dto;

import java.util.List;

import org.springframework.data.domain.Slice;

import waypoint.mvp.plan.application.dto.response.BlockResponse;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanDay;

public record BlockSliceResult(
	Plan plan,
	PlanDay planDay,
	Slice<?> slice,
	List<BlockResponse> contents
) {
}
