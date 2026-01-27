package waypoint.mvp.place.application.dto.llm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PlaceAnalysis(
	@JsonPropertyDescription("전체 내용을 요약한 요약문 (1~2문장)")
	String summary,

	@JsonPropertyDescription("규칙에 따라 생성된 검색 쿼리")
	List<String> searchQueries
) {
}
