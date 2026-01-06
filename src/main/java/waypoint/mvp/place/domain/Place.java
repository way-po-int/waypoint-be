package waypoint.mvp.place.domain;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.BaseTimeEntity;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseTimeEntity {

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

	@Builder(access = AccessLevel.PRIVATE)
	private Place(String name, String address, Point location, PlaceDetail detail) {
		this.name = name;
		this.address = address;
		this.location = location;
		this.detail = detail;
	}

	public static Place create(String name, String address, Point location, PlaceDetail detail) {
		return builder()
			.name(name)
			.address(address)
			.location(location)
			.detail(detail)
			.build();
	}
}
