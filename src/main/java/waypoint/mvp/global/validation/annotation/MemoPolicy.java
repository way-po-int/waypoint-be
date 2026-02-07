package waypoint.mvp.global.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import waypoint.mvp.global.validation.validator.MemoPolicyValidator;

@Documented
@Constraint(validatedBy = MemoPolicyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MemoPolicy {

	String message() default "한글, 영문, 숫자, 이모지, 특수문자(!@#$%^&*()-_+=[]{} ,.?/) 및 줄바꿈만 사용할 수 있습니다.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	boolean nullable() default true;
}
