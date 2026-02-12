package waypoint.mvp.collection.domain;

import org.springframework.data.domain.Sort;

public enum PlaceSortType {
	LATEST(Sort.by(Sort.Direction.DESC, "createdAt")),
	OLDEST(Sort.by(Sort.Direction.ASC, "createdAt"));

	private final Sort sort;

	PlaceSortType(Sort sort) {
		this.sort = sort;
	}

	public Sort getSort() {
		return sort;
	}
}
