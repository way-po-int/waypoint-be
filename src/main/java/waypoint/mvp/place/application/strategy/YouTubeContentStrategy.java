package waypoint.mvp.place.application.strategy;

import java.net.URI;
import java.util.EnumSet;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.place.domain.SocialMediaType;

@Component
@RequiredArgsConstructor
public class YouTubeContentStrategy implements ContentStrategy {

	private static final String VIDEO_MP4 = "video/mp4";

	@Value("classpath:prompts/system_youtube.txt")
	private Resource system;

	@Override
	public EnumSet<SocialMediaType> supportTypes() {
		return EnumSet.of(
			SocialMediaType.YOUTUBE_SHORTS,
			SocialMediaType.YOUTUBE
		);
	}

	@Override
	public SystemMessage getSystemMessage() {
		return SystemMessage.builder()
			.text(system)
			.build();
	}

	@Override
	public UserMessage getUserMessage(String url) {
		Media media = Media.builder()
			.mimeType(MimeType.valueOf(VIDEO_MP4))
			.data(URI.create(url))
			.build();
		return UserMessage.builder()
			.text("")
			.media(media)
			.build();
	}
}
