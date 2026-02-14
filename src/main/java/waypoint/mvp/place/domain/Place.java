package waypoint.mvp.place.domain;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.ExternalIdEntity;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends ExternalIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
	private Point location;

	@Embedded
	private PlaceDetail detail;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private PlaceCategory category;

	@Builder(access = AccessLevel.PRIVATE)
	private Place(String name, String address, Point location, PlaceDetail detail, PlaceCategory category) {
		this.name = name;
		this.address = address;
		this.location = location;
		this.detail = detail;
		this.category = category;
	}

	public static Place create(String name, String address, Point location, PlaceDetail detail,
		PlaceCategory category) {

		return builder()
			.name(name)
			.address(address)
			.location(location)
			.detail(detail)
			.category(category)
			.build();
	}
}
