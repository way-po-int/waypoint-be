package waypoint.mvp.place.application.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/** 장소 이름(쿼리) 목록을 받아 Top1 placeId를 조회하는 DTO */

public record PlaceIdLookupCommand(
	@NotEmpty(message = "queries는 비어 있을 수 없습니다.")
	List<@NotBlank(message = "query는 공백일 수 없습니다.") String> queries
) {
	public static PlaceIdLookupCommand of(List<String> queries) {
		return new PlaceIdLookupCommand(queries);
	}
}
