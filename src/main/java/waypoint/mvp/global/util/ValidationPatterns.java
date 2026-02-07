package waypoint.mvp.global.util;

import java.util.regex.Pattern;

public final class ValidationPatterns {

	private ValidationPatterns() {
	}

	/**
	 * 한글, 영문, 숫자, 공백, 홑따옴표('), 언더바(_) 및 이모지 허용
	 */
	public static final Pattern ALPHANUMERIC_WITH_EMOJI_BASIC =
		Pattern.compile("^[\\p{L}\\p{N}' _\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}\\p{Cs}]*$");

	/**
	 * 한글, 영문, 숫자, 줄바꿈, 이모지 및 특수기호(!@#$%^&*()-_+=[]{},.?/) 허용
	 */
	public static final Pattern ALPHANUMERIC_WITH_EMOJI_EXTENDED =
		Pattern.compile("^[\\p{L}\\p{N}\\r\\n!@#$%^&*()\\-_=+\\[\\]{},.?/\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}\\p{Cs}]*$");
}
