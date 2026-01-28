package waypoint.mvp.place.error.exception;

import lombok.Getter;
import waypoint.mvp.place.domain.ExtractFailureCode;

@Getter
public class ExtractionException extends RuntimeException {

	private final ExtractFailureCode failureCode;

	public ExtractionException(ExtractFailureCode failureCode) {
		this(failureCode, null);
	}

	public ExtractionException(ExtractFailureCode failureCode, Throwable cause) {
		super(failureCode.getMessage(), cause);
		this.failureCode = failureCode;
	}

	public boolean isRetryable() {
		return failureCode.isRetryable();
	}
}
