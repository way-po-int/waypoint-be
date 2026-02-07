package waypoint.mvp.place.error.exception;

import waypoint.mvp.global.error.exception.JobException;
import waypoint.mvp.place.error.ExtractFailureCode;

public class PlaceExtractionException extends JobException {

	public PlaceExtractionException(ExtractFailureCode failureCode, Object... messageDetailArguments) {
		super(failureCode, messageDetailArguments);
	}

	public PlaceExtractionException(ExtractFailureCode failureCode, Throwable cause, Object... messageDetailArguments) {
		super(failureCode, cause, messageDetailArguments);
	}

	@Override
	public ExtractFailureCode getFailureCode() {
		return (ExtractFailureCode)super.getFailureCode();
	}
}
