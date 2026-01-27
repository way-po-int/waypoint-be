package waypoint.mvp.place.util;

import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextUtils {

	private static final Pattern EMOJI_PATTERN = Pattern.compile("[^\\p{L}\\p{N}\\p{P}\\p{Z}]");
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("[\\r\\n]+");
	private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s+");

	public static String clean(String text) {
		if (!StringUtils.hasText(text)) {
			return "";
		}

		String processed;

		// 개행 문자 → 공백
		processed = NEWLINE_PATTERN.matcher(text).replaceAll(" ");

		// 이모지 및 특수 기호 제거
		processed = EMOJI_PATTERN.matcher(processed).replaceAll("");

		// 연속된 공백 제거
		processed = MULTI_SPACE_PATTERN.matcher(processed).replaceAll(" ");
		return processed.trim();
	}
}
