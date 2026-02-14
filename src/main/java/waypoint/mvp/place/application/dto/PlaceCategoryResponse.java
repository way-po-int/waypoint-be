package waypoint.mvp.place.application.dto;

public record PlaceCategoryResponse(
	PlaceCategoryDto level1,
	PlaceCategoryDto level2,
	PlaceCategoryDto level3
) {
}
