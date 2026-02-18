package waypoint.mvp.plan.application.assertion;

import org.assertj.core.api.AbstractAssert;

import waypoint.mvp.plan.application.dto.response.CandidateBlockResponse;

public class CandidateBlockResponseAssert extends AbstractAssert<CandidateBlockResponseAssert, CandidateBlockResponse> {

	protected CandidateBlockResponseAssert(CandidateBlockResponse actual) {
		super(actual, CandidateBlockResponseAssert.class);
	}

	public static CandidateBlockResponseAssert assertThatCandidate(CandidateBlockResponse actual) {
		return new CandidateBlockResponseAssert(actual);
	}

	public CandidateBlockResponseAssert isSelected() {
		isNotNull();
		if (!actual.selected()) {
			failWithMessage("Expected candidate to be selected but was not");
		}
		return this;
	}

	public CandidateBlockResponseAssert isNotSelected() {
		isNotNull();
		if (actual.selected()) {
			failWithMessage("Expected candidate to be unselected but was selected");
		}
		return this;
	}

	public CandidateBlockResponseAssert hasMemo(String expectedMemo) {
		isNotNull();
		if (!expectedMemo.equals(actual.memo())) {
			failWithMessage("Expected memo to be <%s> but was <%s>", expectedMemo, actual.memo());
		}
		return this;
	}

	public CandidateBlockResponseAssert hasPlaceName(String expectedPlaceName) {
		isNotNull();
		if (actual.place() == null) {
			failWithMessage("Expected place to be present but was null");
		}
		if (!expectedPlaceName.equals(actual.place().name())) {
			failWithMessage("Expected place name to be <%s> but was <%s>", expectedPlaceName, actual.place().name());
		}
		return this;
	}

	public CandidateBlockResponseAssert hasNoPlace() {
		isNotNull();
		if (actual.place() != null) {
			failWithMessage("Expected place to be null but was <%s>", actual.place());
		}
		return this;
	}

	public CandidateBlockResponseAssert hasAddedByNickname(String expectedNickname) {
		isNotNull();
		if (actual.addedBy() == null) {
			failWithMessage("Expected addedBy to be present but was null");
		}
		if (!expectedNickname.equals(actual.addedBy().nickname())) {
			failWithMessage("Expected addedBy nickname to be <%s> but was <%s>",
				expectedNickname, actual.addedBy().nickname());
		}
		return this;
	}
}
