package waypoint.mvp.plan.application.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import waypoint.mvp.plan.domain.BlockOpinion;
import waypoint.mvp.plan.domain.BlockOpinionType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpinionSummary(
	int totalCount,
	Distribution distribution,
	MyOpinion my
) {
	public static OpinionSummary from(List<BlockOpinion> opinions, Long userId) {
		int positive = 0;
		int neutral = 0;
		int negative = 0;
		MyOpinion my = null;

		for (BlockOpinion opinion : opinions) {
			switch (opinion.getType()) {
				case POSITIVE -> positive++;
				case NEUTRAL -> neutral++;
				case NEGATIVE -> negative++;
			}
			if (userId != null && opinion.getAddedBy().getUser().getId().equals(userId)) {
				my = new MyOpinion(opinion.getExternalId(), opinion.getType());
			}
		}

		return new OpinionSummary(opinions.size(), new Distribution(positive, neutral, negative), my);
	}

	record Distribution(
		int positive,
		int neutral,
		int negative
	) {
	}

	record MyOpinion(
		String opinionId,
		BlockOpinionType type
	) {
	}
}
