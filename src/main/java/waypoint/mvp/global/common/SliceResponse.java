package waypoint.mvp.global.common;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.data.domain.Slice;

public record SliceResponse<T>(
	List<T> contents,
	boolean hasNext,
	int page,
	int size
) {

	public static <T> SliceResponse<T> from(Slice<T> slice) {
		Objects.requireNonNull(slice, "slice는 null이 될 수 없습니다.");

		return new SliceResponse<>(
			slice.getContent(),
			slice.hasNext(),
			slice.getNumber(),
			slice.getSize()
		);
	}

	public static <T> SliceResponse<T> from(Slice<?> slice, List<T> contents) {
		Objects.requireNonNull(slice, "slice는 null이 될 수 없습니다.");

		return new SliceResponse<>(
			contents,
			slice.hasNext(),
			slice.getNumber(),
			slice.getSize()
		);
	}

	public static <T, E> SliceResponse<T> of(Slice<E> slice, Function<E, T> converter) {
		Objects.requireNonNull(slice, "slice는 null이 될 수 없습니다.");
		Objects.requireNonNull(converter, "converter는 null이 될 수 없습니다.");

		List<T> contents = slice.getContent().stream()
			.map(converter)
			.toList();

		return new SliceResponse<>(
			contents,
			slice.hasNext(),
			slice.getNumber(),
			slice.getSize()
		);
	}
}

