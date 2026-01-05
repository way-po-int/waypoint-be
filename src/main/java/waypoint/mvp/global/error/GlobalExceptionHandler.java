package waypoint.mvp.global.error;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler { // ResponseEntityExceptionHandler

	private static final String ERRORS = "errors";
	private final MessageSource messageSource;

	// 지원하지 않는 HTTP 메서드로 요청했을 때 발생하는 예외
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ProblemDetail handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		return getProblemDetail(e);
	}

	// 서버에서 지원하지 않는 Content-Type으로 요청했을 때 발생하는 예외
	// 예: application/json만 허용하는 API에 application/xml로 요청한 경우
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ProblemDetail handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
		return getProblemDetail(e);
	}

	// 클라이언트가 Accept 헤더에 명시한 응답 미디어 타입을 서버가 생성할 수 없을 때 발생하는 예외
	// 예: 서버는 application/json만 반환 가능한데 Accept: application/xml 요청이 들어온 경우
	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	public ProblemDetail handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException e) {
		return getProblemDetail(e);
	}

	// @RequestParam으로 선언된 필수 쿼리 파라미터가 요청에 포함되지 않았을 때 발생하는 예외
	// 예: /hello 요청 시 ?a=값 이 빠진 경우
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ProblemDetail handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
		return getProblemDetail(e);
	}

	// multipart/form-data 요청에서 필수 @RequestPart가 포함되지 않았을 때 발생하는 예외
	// 예: 파일 업로드 API에 file 파트가 없는 경우
	@ExceptionHandler(MissingServletRequestPartException.class)
	public ProblemDetail handleMissingServletRequestPart(MissingServletRequestPartException e) {
		return getProblemDetail(e);
	}

	// 요청 헤더, 쿠키, 세션 속성 등 @RequestHeader / @CookieValue / @SessionAttribute
	// 바인딩 대상이 요청에 없을 때 발생하는 예외
	@ExceptionHandler(ServletRequestBindingException.class)
	public ProblemDetail handleServletRequestBinding(ServletRequestBindingException e) {
		return getProblemDetail(e);
	}

	// @Valid / @Validated가 적용된 요청 객체(@RequestBody, @ModelAttribute 등)
	// 에 대해 Bean Validation 검증에 실패했을 때 발생하는 예외
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
		return getProblemDetail(e, e.getBindingResult().getFieldErrors().stream()
			.map(error -> new ValidationErrorDetail(
				error.getField(),
				error.getRejectedValue(),
				error.getCode(),
				getErrorMessage(error)
			)).toList()
		);
	}

	// 컨트롤러 메서드 파라미터(@RequestParam, @PathVariable 등)에
	// 적용된 Bean Validation 검증에 실패했을 때 발생하는 예외
	@ExceptionHandler(HandlerMethodValidationException.class)
	public ProblemDetail handleMethodValidation(HandlerMethodValidationException e) {
		return getProblemDetail(e, Stream.concat(
			e.getParameterValidationResults().stream()
				.flatMap(result -> result.getResolvableErrors().stream()
					.map(resolvable -> ValidationErrorDetail.of(
						result.getMethodParameter().getParameterName(),
						result.getArgument(),
						resolvable.getCodes(),
						getErrorMessage(resolvable)
					))),
			e.getCrossParameterValidationResults().stream()
				.map(resolvable -> ValidationErrorDetail.of(
					"<cross-parameter>",
					resolvable.getArguments(),
					resolvable.getCodes(),
					getErrorMessage(resolvable)
				))
		).toList());
	}

	// 해당 리소스를 찾을 수 없을 때 발생하는 예외
	@ExceptionHandler(NoResourceFoundException.class)
	public ProblemDetail handleNoResourceFound(NoResourceFoundException e) {
		return getProblemDetail(e, e.getResourcePath());
	}

	// ProblemDetail을 포함해 명시적으로 HTTP 오류 응답을 만들기 위해 사용하는 예외
	// Spring MVC / WebFlux에서 RFC 9457 기반 오류 응답을 던질 때 사용
	@ExceptionHandler(ErrorResponseException.class)
	public ProblemDetail handleErrorResponse(ErrorResponseException e) {
		return getProblemDetail(e);
	}

	// 요청 파라미터, 경로 변수, 바인딩 대상의 타입 변환에 실패했을 때 발생하는 예외
	// 예: @RequestParam int age 에 "abc"가 전달된 경우
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		return getProblemDetail(
			HttpStatus.BAD_REQUEST,
			getErrorMessage(e.getClass().getCanonicalName(), e.getName(), e.getValue(), e.getRequiredType())
		);
	}

	// HTTP 요청 본문을 읽거나 파싱할 수 없을 때 발생하는 예외
	// 주로 JSON 문법 오류, 잘못된 타입, 요청 본문 누락 시 발생함 (@RequestBody 바인딩 실패)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
		String canonicalName = e.getClass().getCanonicalName();
		Throwable cause = e.getCause();
		if (cause == null) {
			return getProblemDetail(
				HttpStatus.BAD_REQUEST,
				getErrorMessage(canonicalName + "." + "missing")
			);
		}
		return getProblemDetail(HttpStatus.BAD_REQUEST, switch (cause) {
			// JSON 요청 본문을 역직렬화하는 과정에서 값의 형식이나 타입이 대상 필드와 맞지 않을 때 발생하는 Jackson 예외
			// 예: 숫자 필드에 문자열 전달
			case InvalidFormatException ife -> getErrorMessage(
				canonicalName + "." + ife.getClass().getSimpleName(),
				ife.getPath().getFirst().getFieldName(),
				ife.getValue(),
				ife.getTargetType()
			);

			// JSON 요청 본문의 구조(객체/배열/필드)가 서버에서 기대한 구조와 일치하지 않을 때 발생하는 Jackson 예외
			// 예: 객체를 기대했는데 배열이 온 경우
			case MismatchedInputException mie -> getErrorMessage(
				canonicalName + "." + mie.getClass().getSimpleName(),
				mie.getPath().getFirst().getFieldName(),
				mie.getTargetType()
			);

			// 요청 본문을 읽을 수 없는 경우
			default -> getErrorMessage(canonicalName + "." + "unreadable");
		});
	}

	// Bean Validation(@NotNull, @Size, @Max 등) 제약 조건을 위반했을 때 발생하는 예외
	// 주로 @Validated가 적용된 메서드 파라미터나 PathVariable, RequestParam 검증 실패 시 발생
	@ExceptionHandler(ConstraintViolationException.class)
	public ProblemDetail handleConstraintViolation(ConstraintViolationException e) {
		ProblemDetail problemDetail = getProblemDetail(
			HttpStatus.BAD_REQUEST,
			getErrorMessage(e.getClass().getCanonicalName())
		);
		problemDetail.setProperty(ERRORS, e.getConstraintViolations().stream()
			.map(violation -> {
				String propertyPath = violation.getPropertyPath().toString();
				return new ValidationErrorDetail(
					propertyPath.substring(propertyPath.lastIndexOf('.') + 1),
					violation.getInvalidValue(),
					violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
					violation.getMessage()
				);
			})
		);
		return problemDetail;
	}

	// 비동기 요청이 이미 완료되었거나 타임아웃/에러로 종료된 이후에 다시 응답을 쓰려고 할 때 발생하는 예외
	@ExceptionHandler(AsyncRequestNotUsableException.class)
	public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException e) {
		log.trace("비동기 요청 처리 중 연결이 이미 종료됨: {}", e.getMessage());
	}

	// 예상하지 못한 서버 오류
	@ExceptionHandler(Exception.class)
	public ProblemDetail handleException(Exception e, HttpServletRequest request) {
		log.error("예상하지 못한 오류 발생: {} {}", request.getMethod(), request.getRequestURI(), e);
		return getProblemDetail(
			HttpStatus.INTERNAL_SERVER_ERROR,
			getErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.name())
		);
	}

	private ProblemDetail getProblemDetail(HttpStatusCode statusCode, String detail) {
		return ProblemDetail.forStatusAndDetail(statusCode, detail);
	}

	private ProblemDetail getProblemDetail(ErrorResponse errorResponse) {
		return errorResponse.updateAndGetBody(messageSource, LocaleContextHolder.getLocale());
	}

	private ProblemDetail getProblemDetail(ErrorResponse errorResponse, String detail) {
		ProblemDetail problemDetail = getProblemDetail(errorResponse);
		problemDetail.setDetail(getErrorMessage(errorResponse.getDetailMessageCode(), detail));
		return problemDetail;
	}

	private ProblemDetail getProblemDetail(ErrorResponse errorResponse, Object errors) {
		ProblemDetail problemDetail = getProblemDetail(errorResponse);
		problemDetail.setDetail(getErrorMessage(errorResponse.getDetailMessageCode()));
		problemDetail.setProperty(ERRORS, errors);
		return problemDetail;
	}

	private String getErrorMessage(MessageSourceResolvable resolvable) {
		return messageSource.getMessage(resolvable, LocaleContextHolder.getLocale());
	}

	private String getErrorMessage(String code, Object... args) {
		return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	record ValidationErrorDetail(String field, Object value, String code, String reason) {

		static ValidationErrorDetail of(String field, Object value, @Nullable String[] codes, String reason) {
			return new ValidationErrorDetail(
				field,
				value,
				Optional.ofNullable(codes)
					.filter(c -> c.length > 0)
					.map(c -> c[c.length - 1])
					.orElse(null),
				reason
			);
		}
	}
}
