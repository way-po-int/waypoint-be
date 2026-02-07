package waypoint.mvp.global.error.exception;

import lombok.Getter;
import waypoint.mvp.global.error.JobFailureCode;

@Getter
public abstract class JobException extends RuntimeException {

	private final JobFailureCode failureCode;

	protected JobException(JobFailureCode failureCode, Object... messageDetailArguments) {
		this(failureCode, null, messageDetailArguments);
	}

	protected JobException(JobFailureCode failureCode, Throwable cause, Object... messageDetailArguments) {
		super(failureCode.getMessage(messageDetailArguments), cause);
		this.failureCode = failureCode;
	}

	public boolean isRetryable() {
		return failureCode.isRetryable();
	}
}
