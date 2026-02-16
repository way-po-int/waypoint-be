package waypoint.mvp.plan.application.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanDay;

public record BlockListResponse(
	DayInfoResponse dayInfo,
	List<BlockResponse> contents,
	boolean hasNext,
	int page,
	int size
) {
	public static BlockListResponse from(
		PlanDay planDay,
		Plan plan,
		Slice<?> slice,
		List<BlockResponse> contents
	) {
		return new BlockListResponse(
			DayInfoResponse.from(planDay, plan),
			contents,
			slice.hasNext(),
			slice.getNumber(),
			slice.getSize()
		);
	}
}
