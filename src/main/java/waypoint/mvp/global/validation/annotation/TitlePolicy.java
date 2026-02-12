package waypoint.mvp.global.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import waypoint.mvp.global.validation.validator.TitlePolicyValidator;

@Documented
@Constraint(validatedBy = TitlePolicyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TitlePolicy {
	String message() default "한글, 영문, 숫자, 특수문자(' _), 이모지만 사용할 수 있습니다.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	boolean nullable() default false;
}
