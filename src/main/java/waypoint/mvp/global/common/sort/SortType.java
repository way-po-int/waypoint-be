package waypoint.mvp.global.common.sort;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public enum SortType {
	CREATED_AT_DESC("createdAt", Sort.Direction.DESC),
	CREATED_AT_ASC("createdAt", Sort.Direction.ASC),
	UPDATED_AT_DESC("updatedAt", Sort.Direction.DESC),
	UPDATED_AT_ASC("updatedAt", Sort.Direction.ASC);

	private final String property;
	private final Sort.Direction direction;

	SortType(String property, Sort.Direction direction) {
		this.property = property;
		this.direction = direction;
	}

	public String getProperty() {
		return property;
	}

	public Sort.Direction getDirection() {
		return direction;
	}

	public Sort toSort() {
		return Sort.by(direction, property);
	}

	public Pageable toPageable(int page, int size) {
		return PageRequest.of(page, size, toSort());
	}

	public Pageable toPageable(Pageable pageable) {
		return PageRequest.of(
			pageable.getPageNumber(),
			pageable.getPageSize(),
			toSort()
		);
	}
}
