package waypoint.mvp.global.common.sort;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class SortablePageRequest {

	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 20;

	private SortablePageRequest() {
	}

	public static Pageable of(SortType sortType) {
		return sortType.toPageable(DEFAULT_PAGE, DEFAULT_SIZE);
	}

	public static Pageable of(SortType sortType, int page, int size) {
		return sortType.toPageable(page, size);
	}

	public static Pageable of(Pageable pageable, SortType sortType) {
		return PageRequest.of(
			pageable.getPageNumber(),
			pageable.getPageSize(),
			sortType.toSort()
		);
	}
}
