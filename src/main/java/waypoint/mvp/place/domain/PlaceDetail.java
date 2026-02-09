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

	@Column(length = 100)
	private String primaryType;

	@Column(length = 2048)
	private String googleMapsUri;

	@Column(length = 512)
	private String photoName;
	// 캐시용 필드
	@Column(length = 2048)
	private String photoUri;

	@Builder(access = AccessLevel.PRIVATE)
	private PlaceDetail(
		String placeId,
		String primaryType,
		String googleMapsUri,
		String photoName,
		String photoUri
	) {
		this.placeId = placeId;
		this.primaryType = primaryType;
		this.googleMapsUri = googleMapsUri;
		this.photoName = photoName;
		this.photoUri = photoUri;
	}

	public static PlaceDetail create(String placeId) {
		return builder()
			.placeId(placeId)
			.build();
	}

	public static PlaceDetail create(
		String placeId,
		String primaryType,
		String googleMapsUri,
		String photoName
	) {
		return builder()
			.placeId(placeId)
			.primaryType(primaryType)
			.googleMapsUri(googleMapsUri)
			.photoName(photoName)
			.build();
	}

	public void updatePhotoUri(String photoUri) {
		this.photoUri = photoUri;
	}
}
