package waypoint.mvp.place.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_category_mappings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceCategoryMapping {

	@Id
	private String primaryType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private PlaceCategory category;
}
