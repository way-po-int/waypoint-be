package waypoint.mvp.place.domain;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.error.SocialMediaError;

@RequiredArgsConstructor
public enum SocialMediaType {
	YOUTUBE_SHORTS(List.of("youtube.com/shorts/")),
	YOUTUBE(List.of("youtube.com/", "youtu.be/"));

	private final List<String> supports;

	public static SocialMediaType from(String url) {
		try {
			URI uri = URI.create(url);
			String u = uri.getHost() + uri.getPath();
			return Arrays.stream(values())
				.filter(type -> type.isSupported(u))
				.findFirst()
				.orElseThrow();
		} catch (IllegalArgumentException | NullPointerException | NoSuchElementException e) {
			throw new BusinessException(SocialMediaError.SOCIAL_MEDIA_UNSUPPORTED);
		}
	}

	private boolean isSupported(String url) {
		return supports.stream()
			.anyMatch(url::contains);
	}
}
