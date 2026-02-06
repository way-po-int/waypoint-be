package waypoint.mvp.place.domain;

import java.util.List;

public enum SocialMediaStatus {
	PENDING,
	EXTRACTING,
	SEARCHING,
	COMPLETED,
	FAILED;

	public static final List<SocialMediaStatus> IN_PROGRESS = List.of(
		PENDING, EXTRACTING, SEARCHING);

	public boolean isFinished() {
		return !IN_PROGRESS.contains(this);
	}
}
