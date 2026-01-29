package waypoint.mvp.place.domain;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.springframework.util.ObjectUtils;

import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.error.SocialMediaError;

public enum SocialMediaType {
	YOUTUBE_SHORTS("^https://(www\\.|m\\.)?youtube\\.com/shorts/.*"),
	YOUTUBE("^https://((www\\.|m\\.)?youtube\\.com/(watch\\?v=|embed/|v/)|youtu\\.be/).*");

	private final Pattern pattern;

	SocialMediaType(String regex) {
		this.pattern = Pattern.compile(regex);
	}

	public static SocialMediaType from(String url) {
		if (ObjectUtils.isEmpty(url)) {
			throw new BusinessException(SocialMediaError.SOCIAL_MEDIA_UNSUPPORTED);
		}
		return Arrays.stream(values())
			.filter(type -> type.isSupported(url))
			.findFirst()
			.orElseThrow(() -> new BusinessException(SocialMediaError.SOCIAL_MEDIA_UNSUPPORTED));
	}

	private boolean isSupported(String url) {
		return pattern.matcher(url).matches();
	}
}
