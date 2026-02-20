package waypoint.mvp.place.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.ExternalIdEntity;

@Entity
@Table(name = "manual_places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ManualPlace extends ExternalIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false)
	private String categoryId;

	@Column(length = 2048)
	private String socialMediaUrl;

	@Builder(access = AccessLevel.PRIVATE)
	private ManualPlace(String name, String address, String categoryId, String socialMediaUrl) {
		this.name = name;
		this.address = address;
		this.categoryId = categoryId;
		this.socialMediaUrl = socialMediaUrl;
	}

	public static ManualPlace create(String name, String address, String socialMediaUrl) {
		return builder()
			.name(name)
			.categoryId("")
			.address(address)
			.socialMediaUrl(socialMediaUrl)
			.build();
	}
}
