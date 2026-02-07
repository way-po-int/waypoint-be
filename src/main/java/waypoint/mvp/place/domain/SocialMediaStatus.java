package waypoint.mvp.place.domain;

import java.util.List;

public enum SocialMediaStatus {
	PENDING,
	EXTRACTING,
	SEARCHING,
	COMPLETED,
	FAILED,
	RETRY_WAITING;

	public static final List<SocialMediaStatus> IN_PROGRESS = List.of(
		PENDING, EXTRACTING, SEARCHING, RETRY_WAITING);

	public boolean isFinished() {
		return !IN_PROGRESS.contains(this);
	}
}
