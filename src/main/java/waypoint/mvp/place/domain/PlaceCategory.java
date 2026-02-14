package waypoint.mvp.place.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
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
@Table(name = "place_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceCategory {

	@Id
	private Long id;

	@Column
	private String name;

	@Column
	private Integer depth;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private PlaceCategory parent;

	public List<Long> getCategoryPathIds() {
		List<Long> pathIds = new ArrayList<>();

		if (this.depth >= 1) {
			pathIds.add((this.id / 100000) * 100000);
		}
		if (this.depth >= 2) {
			pathIds.add((this.id / 1000) * 1000);
		}
		if (this.depth >= 3) {
			pathIds.add(this.id);
		}
		return pathIds;
	}
}
