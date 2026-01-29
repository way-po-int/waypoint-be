package waypoint.mvp.place.domain.content;

import java.util.List;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YouTubeContentSnapshot extends ContentSnapshot {

	private String description;
	private List<String> tags;
	private String comment;
	private String channelId;
	private String channelTitle;
	private String duration;
}
