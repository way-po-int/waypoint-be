package waypoint.mvp.place.domain;

import java.util.List;

public enum PlaceSearchStatus {
	PENDING,
	PROCESSING,
	COMPLETED,
	NOT_FOUND,
	FAILED,
	RETRY_WAITING;

	public static final List<PlaceSearchStatus> IN_PROGRESS = List.of(
		PENDING, PROCESSING, RETRY_WAITING);

	public boolean isFinished() {
		return !IN_PROGRESS.contains(this);
	}
}
