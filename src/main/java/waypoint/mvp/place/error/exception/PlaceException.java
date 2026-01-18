package waypoint.mvp.place.error.exception;

import lombok.Getter;
import waypoint.mvp.place.error.PlaceError;

@Getter
public class PlaceException extends RuntimeException {
	private final PlaceError error;

	public PlaceException(PlaceError error) {
		super(error.getMessage());
		this.error = error;
	}
}
