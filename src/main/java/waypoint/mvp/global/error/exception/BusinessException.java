package waypoint.mvp.global.error.exception;

import java.util.Map;

import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponseException;

import lombok.Getter;
import waypoint.mvp.global.error.ErrorCode;

@Getter
public class BusinessException extends ErrorResponseException {

	public BusinessException(ErrorCode errorCode, Object... messageDetailArguments) {
		this(errorCode, null, messageDetailArguments);
	}

	public BusinessException(ErrorCode errorCode, Throwable cause, Object... messageDetailArguments) {
		super(
			errorCode.getHttpStatus(),
			ProblemDetail.forStatusAndDetail(
				errorCode.getHttpStatus(),
				errorCode.getMessage(messageDetailArguments)
			),
			cause,
			null,
			messageDetailArguments
		);
		super.getBody().setProperty("code", errorCode.name());
	}

	public BusinessException addProperty(String name, @Nullable Object value) {
		super.getBody().setProperty(name, value);
		return this;
	}

	public BusinessException addProperties(@Nullable Map<String, Object> properties) {
		super.getBody().setProperties(properties);
		return this;
	}
}
