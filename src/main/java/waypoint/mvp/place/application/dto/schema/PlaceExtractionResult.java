package waypoint.mvp.place.application.dto.schema;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PlaceExtractionResult(
	@JsonPropertyDescription("전체 내용을 요약한 요약문")
	String summary,

	@JsonPropertyDescription("규칙에 따라 생성된 검색 쿼리")
	List<String> searchQueries
) {
}
