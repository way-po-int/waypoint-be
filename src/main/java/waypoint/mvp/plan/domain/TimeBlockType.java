package waypoint.mvp.plan.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeBlockType {
	PLACE("장소 블록"),
	FREE("자유시간 블록");

	private final String description;

	public boolean isPlace() {
		return this == PLACE;
	}
}
