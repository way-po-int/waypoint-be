package waypoint.mvp.place.error.exception;

import waypoint.mvp.global.error.exception.JobException;
import waypoint.mvp.place.error.SearchFailureCode;

public class PlaceSearchException extends JobException {

	public PlaceSearchException(SearchFailureCode failureCode, Object... messageDetailArguments) {
		super(failureCode, messageDetailArguments);
	}

	@Override
	public SearchFailureCode getFailureCode() {
		return (SearchFailureCode)super.getFailureCode();
	}
}
