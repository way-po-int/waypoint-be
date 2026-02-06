package waypoint.mvp.global.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import waypoint.mvp.global.util.ValidationPatterns;
import waypoint.mvp.global.validation.annotation.TitlePolicy;

public class TitlePolicyValidator implements ConstraintValidator<TitlePolicy, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}

		return ValidationPatterns.ALPHANUMERIC_WITH_EMOJI_BASIC.matcher(value).matches();
	}
}
