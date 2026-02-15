package waypoint.mvp.plan.application.dto.response;

import java.util.List;

import waypoint.mvp.plan.domain.BlockOpinion;

public record OpinionSummary(
	int positive,
	int neutral,
	int negative
) {
	public static OpinionSummary from(List<BlockOpinion> opinions) {
		int positive = 0;
		int neutral = 0;
		int negative = 0;

		for (BlockOpinion opinion : opinions) {
			switch (opinion.getType()) {
				case POSITIVE -> positive++;
				case NEUTRAL -> neutral++;
				case NEGATIVE -> negative++;
			}
		}

		return new OpinionSummary(positive, neutral, negative);
	}
}
