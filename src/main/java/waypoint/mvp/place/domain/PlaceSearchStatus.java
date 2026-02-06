package waypoint.mvp.place.domain;

public enum PlaceSearchStatus {
	PENDING,
	PROCESSING,
	COMPLETED,
	NOT_FOUND,
	FAILED,
	RETRY_WAITING
}
