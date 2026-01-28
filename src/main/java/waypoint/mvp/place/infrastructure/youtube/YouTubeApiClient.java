package waypoint.mvp.place.infrastructure.youtube;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoContentDetails;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;

import waypoint.mvp.place.application.dto.content.YouTubeRawContent;
import waypoint.mvp.place.domain.ExtractFailureCode;
import waypoint.mvp.place.error.exception.ExtractionException;
import waypoint.mvp.place.util.TextUtils;

@Component
public class YouTubeApiClient {

	private static final Pattern VIDEO_ID_PATTERN = Pattern.compile(
		"(?:youtube\\.com/(?:watch\\?v=|embed/|v/|shorts/)|youtu\\.be/)([a-zA-Z0-9_-]{11})");

	private final YouTube youTube;

	public YouTubeApiClient(@Value("${google.youtube.api-key}") String apiKey) {
		try {
			YouTubeRequestInitializer initializer = new YouTubeRequestInitializer(apiKey);
			this.youTube = new YouTube.Builder(
				GoogleNetHttpTransport.newTrustedTransport(),
				GsonFactory.getDefaultInstance(),
				null
			).setYouTubeRequestInitializer(initializer).build();
		} catch (GeneralSecurityException | IOException e) {
			throw new IllegalStateException("YouTube 클라이언트 초기화 실패", e);
		}
	}

	public YouTubeRawContent getRawContent(String url) {
		String videoId = extractVideoId(url);

		try {
			YouTube.Videos.List request = youTube.videos()
				.list(List.of("snippet", "contentDetails"))
				.setId(List.of(videoId));

			VideoListResponse response = request.execute();
			List<Video> items = response.getItems();
			if (items.isEmpty()) {
				throw new ExtractionException(ExtractFailureCode.CONTENT_NOT_FOUND);
			}

			VideoSnippet snippet = items.getFirst().getSnippet();
			VideoContentDetails contentDetails = items.getFirst().getContentDetails();

			String title = Objects.requireNonNullElse(snippet.getTitle(), "");
			String description = TextUtils.clean(snippet.getDescription());
			List<String> tags = Objects.requireNonNullElse(snippet.getTags(), Collections.emptyList());
			String comment = getCreatorCommentOnly(videoId, snippet.getChannelId());

			return new YouTubeRawContent(
				url,
				videoId,
				title,
				description,
				tags,
				comment,
				snippet.getChannelId(),
				snippet.getChannelTitle(),
				contentDetails.getDuration()
			);
		} catch (IOException e) {
			throw new ExtractionException(ExtractFailureCode.YOUTUBE_API_ERROR, e);
		}
	}

	private String getCreatorCommentOnly(String videoId, String channelId) {
		try {
			YouTube.CommentThreads.List request = youTube.commentThreads()
				.list(List.of("snippet"))
				.setVideoId(videoId)
				.setTextFormat("plainText")
				.setOrder("relevance")
				.setMaxResults(5L);

			CommentThreadListResponse response = request.execute();

			return response.getItems().stream()
				.map(thread -> thread.getSnippet().getTopLevelComment().getSnippet())
				.filter(snippet -> isCreatorComment(snippet, channelId))
				.map(CommentSnippet::getTextDisplay)
				.map(TextUtils::clean)
				.collect(Collectors.joining(" "));
		} catch (IOException e) {
			// 댓글이 막혀있으면 403 발생
			return "";
		}
	}

	private boolean isCreatorComment(CommentSnippet snippet, String channelId) {
		if (snippet.getAuthorChannelId() == null) {
			return false;
		}
		return channelId.equals(snippet.getAuthorChannelId().getValue());
	}

	private String extractVideoId(String url) {
		Matcher matcher = VIDEO_ID_PATTERN.matcher(url);

		if (matcher.find()) {
			return matcher.group(1);
		}

		throw new ExtractionException(ExtractFailureCode.CONTENT_NOT_FOUND);
	}
}
