package waypoint.mvp.global.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import waypoint.mvp.global.util.ValidationPatterns;
import waypoint.mvp.global.validation.annotation.MemoPolicy;

public class MemoPolicyValidator implements ConstraintValidator<MemoPolicy, String> {

	private boolean allowNullable;

	@Override
	public void initialize(MemoPolicy constraintAnnotation) {
		this.allowNullable = constraintAnnotation.nullable();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return allowNullable;
		}

		return ValidationPatterns.ALPHANUMERIC_WITH_EMOJI_EXTENDED.matcher(value).matches();
	}
}
