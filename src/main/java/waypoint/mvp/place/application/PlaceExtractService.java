package waypoint.mvp.place.application;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.dto.content.RawContent;
import waypoint.mvp.place.application.dto.llm.PlaceAnalysis;
import waypoint.mvp.place.application.dto.llm.PlaceExtractionResult;
import waypoint.mvp.place.application.strategy.ContentStrategy;
import waypoint.mvp.place.domain.SocialMediaType;
import waypoint.mvp.place.error.SocialMediaError;

@Service
@RequiredArgsConstructor
public class PlaceExtractService {

	private final List<ContentStrategy<?>> contentStrategies;
	private final ChatClient chatClient;

	public PlaceExtractionResult extract(SocialMediaType type, String url) {
		var strategy = contentStrategies.stream()
			.filter(s -> s.support(type))
			.findFirst()
			.orElseThrow(() -> new BusinessException(SocialMediaError.SOCIAL_MEDIA_UNSUPPORTED));

		return process(strategy, url);
	}

	private <T extends RawContent> PlaceExtractionResult process(ContentStrategy<T> strategy, String url) {
		T rawContent = strategy.fetch(url);

		PlaceAnalysis analysis = chatClient.prompt()
			.messages(
				strategy.getSystemMessage(),
				strategy.getUserMessage(rawContent))
			.call()
			.entity(PlaceAnalysis.class);

		return new PlaceExtractionResult(rawContent, analysis);
	}
}
