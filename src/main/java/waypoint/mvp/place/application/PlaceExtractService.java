package waypoint.mvp.place.application;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.dto.schema.PlaceExtractionResult;
import waypoint.mvp.place.application.strategy.ContentStrategy;
import waypoint.mvp.place.domain.ExtractFailureCode;
import waypoint.mvp.place.domain.SocialMediaType;
import waypoint.mvp.place.error.SocialMediaError;
import waypoint.mvp.place.error.exception.ExtractionException;

@Service
@RequiredArgsConstructor
public class PlaceExtractService {

	private final List<ContentStrategy> contentStrategies;
	private final ChatClient chatClient;

	public PlaceExtractionResult extract(SocialMediaType type, String url) {
		ContentStrategy strategy = contentStrategies.stream()
			.filter(s -> s.support(type))
			.findFirst()
			.orElseThrow(() -> new BusinessException(SocialMediaError.SOCIAL_MEDIA_UNSUPPORTED));

		try {
			return chatClient.prompt()
				.messages(strategy.getSystemMessage(), strategy.getUserMessage(url))
				.call()
				.entity(PlaceExtractionResult.class);
		} catch (ExtractionException e) {
			throw e;
		} catch (Exception e) {
			throw new ExtractionException(ExtractFailureCode.GENAI_ERROR, e);
		}
	}
}
