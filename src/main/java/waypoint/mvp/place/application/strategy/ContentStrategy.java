package waypoint.mvp.place.application.strategy;

import java.util.EnumSet;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import waypoint.mvp.place.domain.SocialMediaType;

public interface ContentStrategy {

	EnumSet<SocialMediaType> supportTypes();

	SystemMessage getSystemMessage();

	UserMessage getUserMessage(String url);

	default boolean support(SocialMediaType type) {
		return supportTypes().contains(type);
	}
}
