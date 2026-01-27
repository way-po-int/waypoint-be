package waypoint.mvp.place.application.strategy;

import java.util.EnumSet;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import waypoint.mvp.place.application.dto.content.RawContent;
import waypoint.mvp.place.domain.SocialMediaType;

public interface ContentStrategy<T extends RawContent> {

	EnumSet<SocialMediaType> supportTypes();

	T fetch(String url);

	SystemMessage getSystemMessage();

	UserMessage getUserMessage(T rawContent);

	default boolean support(SocialMediaType type) {
		return supportTypes().contains(type);
	}
}
