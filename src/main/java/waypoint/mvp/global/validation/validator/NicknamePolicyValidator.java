package waypoint.mvp.global.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import waypoint.mvp.global.util.ValidationPatterns;
import waypoint.mvp.global.validation.annotation.NicknamePolicy;

public class NicknamePolicyValidator implements ConstraintValidator<NicknamePolicy, String> {

	private boolean nullable;

	@Override
	public void initialize(NicknamePolicy constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return nullable;
		}
		if (value.isBlank()) {
			return false;
		}

		int length = value.codePointCount(0, value.length());
		if (length < 2 || length > 10) {
			return false;
		}

		return value.matches(ValidationPatterns.NICKNAME_REGEX);
	}
}
