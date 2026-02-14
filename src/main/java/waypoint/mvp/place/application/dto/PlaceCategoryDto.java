package waypoint.mvp.place.application.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import waypoint.mvp.place.domain.PlaceCategory;

public record PlaceCategoryDto(
	String categoryId,
	String name,

	@JsonIgnore
	Integer depth,

	@JsonIgnore
	List<Long> pathIds
) {

	public static PlaceCategoryDto from(PlaceCategory category) {
		return new PlaceCategoryDto(
			String.valueOf(category.getId()),
			category.getName(),
			category.getDepth(),
			category.getCategoryPathIds()
		);
	}
}
