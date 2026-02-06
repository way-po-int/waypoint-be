package waypoint.mvp.global.validation.validator;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import waypoint.mvp.global.validation.annotation.TitlePolicy;

public class TitlePolicyValidator implements ConstraintValidator<TitlePolicy, String> {

	private static final Pattern PATTERN = Pattern.compile("^[\\p{L}\\p{N}' _\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}\\p{Cs}]*$");

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}

		return PATTERN.matcher(value).matches();
	}
}
