package waypoint.mvp.global.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import waypoint.mvp.global.validation.validator.NicknamePolicyValidator;

@Documented
@Constraint(validatedBy = NicknamePolicyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NicknamePolicy {

	String message() default "닉네임은 2~10자의 한글/영문/숫자만 가능합니다. (공백/특수문자 불가)";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	boolean nullable() default false;
}
