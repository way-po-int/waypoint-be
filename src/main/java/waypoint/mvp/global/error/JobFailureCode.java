package waypoint.mvp.global.error;

public interface JobFailureCode {
	String name();

	String getMessage();

	boolean isRetryable();

	default String getMessage(Object... args) {
		return getMessage().formatted(args);
	}
}
