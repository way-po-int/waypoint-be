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
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.place.application.dto.content.YouTubeRawContent;
import waypoint.mvp.place.domain.SocialMediaType;
import waypoint.mvp.place.infrastructure.youtube.YouTubeApiClient;

@Component
@RequiredArgsConstructor
public class YouTubeContentStrategy implements ContentStrategy<YouTubeRawContent> {

	private static final String VIDEO_MP4 = "video/mp4";

	private final YouTubeApiClient apiClient;

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
	public YouTubeRawContent fetch(String url) {
		return apiClient.getRawContent(url);
	}

	@Override
	public SystemMessage getSystemMessage() {
		return SystemMessage.builder()
			.text(system)
			.build();
	}

	@Override
	public UserMessage getUserMessage(YouTubeRawContent rawContent) {
		Media media = Media.builder()
			.mimeType(MimeType.valueOf(VIDEO_MP4))
			.data(URI.create(rawContent.url()))
			.build();
		return UserMessage.builder()
			.text(generateRawText(rawContent))
			.media(media)
			.build();
	}

	private String generateRawText(YouTubeRawContent content) {
		StringBuilder sb = new StringBuilder()
			.append("[영상 제목] ").append(content.title());

		if (StringUtils.hasText(content.description())) {
			sb.append(" [설명] ").append(content.description());
		}
		if (!ObjectUtils.isEmpty(content.tags())) {
			sb.append(" [태그] ").append(content.tags());
		}
		if (StringUtils.hasText(content.comment())) {
			sb.append(" [댓글] ").append(content.comment());
		}

		return sb.toString();
	}
}
