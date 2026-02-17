package waypoint.mvp.plan.application.assertion;

import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.AbstractAssert;

import waypoint.mvp.plan.application.dto.response.BlockResponse;
import waypoint.mvp.plan.application.dto.response.CandidateBlockResponse;
import waypoint.mvp.plan.domain.BlockStatus;
import waypoint.mvp.plan.domain.TimeBlockType;

public class BlockResponseAssert extends AbstractAssert<BlockResponseAssert, BlockResponse> {

	protected BlockResponseAssert(BlockResponse actual) {
		super(actual, BlockResponseAssert.class);
	}

	public static BlockResponseAssert assertThatBlock(BlockResponse actual) {
		return new BlockResponseAssert(actual);
	}

	public BlockResponseAssert hasStatus(BlockStatus expected) {
		isNotNull();
		if (actual.blockStatus() != expected) {
			failWithMessage("Expected blockStatus to be <%s> but was <%s>", expected, actual.blockStatus());
		}
		return this;
	}

	public BlockResponseAssert hasType(TimeBlockType expected) {
		isNotNull();
		if (actual.type() != expected) {
			failWithMessage("Expected type to be <%s> but was <%s>", expected, actual.type());
		}
		return this;
	}

	public BlockResponseAssert hasCandidateCount(int expected) {
		isNotNull();
		if (actual.candidateCount() != expected) {
			failWithMessage("Expected candidateCount to be <%s> but was <%s>", expected, actual.candidateCount());
		}
		return this;
	}

	public BlockResponseAssert hasStartTime(LocalTime expected) {
		isNotNull();
		if (!actual.startTime().equals(expected)) {
			failWithMessage("Expected startTime to be <%s> but was <%s>", expected, actual.startTime());
		}
		return this;
	}

	public BlockResponseAssert hasEndTime(LocalTime expected) {
		isNotNull();
		if (!actual.endTime().equals(expected)) {
			failWithMessage("Expected endTime to be <%s> but was <%s>", expected, actual.endTime());
		}
		return this;
	}

	public BlockResponseAssert hasSelectedBlock() {
		isNotNull();
		if (actual.selectedBlock() == null) {
			failWithMessage("Expected selectedBlock to be present but was null");
		}
		return this;
	}

	public BlockResponseAssert hasNoSelectedBlock() {
		isNotNull();
		if (actual.selectedBlock() != null) {
			failWithMessage("Expected selectedBlock to be null but was <%s>", actual.selectedBlock());
		}
		return this;
	}

	public CandidateBlockResponseAssert selectedBlock() {
		isNotNull();
		hasSelectedBlock();
		return CandidateBlockResponseAssert.assertThatCandidate(actual.selectedBlock());
	}

	public BlockResponseAssert hasSelectedBlockWithMemo(String expectedMemo) {
		hasSelectedBlock();
		CandidateBlockResponse selected = actual.selectedBlock();
		if (!expectedMemo.equals(selected.memo())) {
			failWithMessage("Expected selectedBlock memo to be <%s> but was <%s>", expectedMemo, selected.memo());
		}
		return this;
	}

	public BlockResponseAssert hasSelectedBlockWithPlaceName(String expectedPlaceName) {
		hasSelectedBlock();
		CandidateBlockResponse selected = actual.selectedBlock();
		if (selected.place() == null) {
			failWithMessage("Expected selectedBlock to have place but was null");
		}
		if (!expectedPlaceName.equals(selected.place().name())) {
			failWithMessage("Expected selectedBlock place name to be <%s> but was <%s>",
				expectedPlaceName, selected.place().name());
		}
		return this;
	}

	public BlockResponseAssert hasSelectedBlockAddedBy(String expectedNickname) {
		hasSelectedBlock();
		CandidateBlockResponse selected = actual.selectedBlock();
		if (!expectedNickname.equals(selected.addedBy().nickname())) {
			failWithMessage("Expected selectedBlock addedBy nickname to be <%s> but was <%s>",
				expectedNickname, selected.addedBy().nickname());
		}
		return this;
	}

	public BlockResponseAssert hasCandidatesWithPlaceNames(String... expectedPlaceNames) {
		isNotNull();
		List<String> actualPlaceNames = actual.candidates().stream()
			.map(c -> c.place().name())
			.toList();

		if (actualPlaceNames.size() != expectedPlaceNames.length) {
			failWithMessage("Expected <%s> candidates but found <%s>",
				expectedPlaceNames.length, actualPlaceNames.size());
		}

		for (String expectedName : expectedPlaceNames) {
			if (!actualPlaceNames.contains(expectedName)) {
				failWithMessage("Expected candidates to contain place name <%s> but was not found. Actual: %s",
					expectedName, actualPlaceNames);
			}
		}
		return this;
	}

	public BlockResponseAssert allCandidatesUnselected() {
		isNotNull();
		boolean anySelected = actual.candidates().stream().anyMatch(CandidateBlockResponse::selected);
		if (anySelected) {
			failWithMessage("Expected all candidates to be unselected but found selected candidate(s)");
		}
		return this;
	}

	public BlockResponseAssert allCandidatesAddedBy(String expectedNickname) {
		isNotNull();
		boolean allMatch = actual.candidates().stream()
			.allMatch(c -> expectedNickname.equals(c.addedBy().nickname()));
		if (!allMatch) {
			failWithMessage("Expected all candidates to be added by <%s>", expectedNickname);
		}
		return this;
	}
}
