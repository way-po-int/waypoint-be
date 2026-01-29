package waypoint.mvp.place.application.dto.content;

import java.time.Duration;
import java.util.List;

import waypoint.mvp.place.domain.content.ContentSnapshot;
import waypoint.mvp.place.domain.content.YouTubeContentSnapshot;

public record YouTubeRawContent(
	String url,
	String videoId,
	String title,
	String description,
	List<String> tags,
	String comment,
	String channelId,
	String channelTitle,
	String duration
) implements RawContent {

	@Override
	public ContentSnapshot toSnapshot() {
		return YouTubeContentSnapshot.builder()
			.contentId(videoId)
			.title(title)
			.description(description)
			.tags(tags)
			.comment(comment)
			.channelId(channelId)
			.channelTitle(channelTitle)
			.duration(duration)
			.build();
	}

	public Duration getDuration() {
		return Duration.parse(duration);
	}
}
