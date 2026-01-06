package waypoint.mvp.place.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceDetail {

	@Column(nullable = false, unique = true)
	private String placeId;

	@Builder(access = AccessLevel.PRIVATE)
	private PlaceDetail(String placeId) {
		this.placeId = placeId;
	}

	public static PlaceDetail create(String placeId) {
		return builder()
			.placeId(placeId)
			.build();
	}
}
