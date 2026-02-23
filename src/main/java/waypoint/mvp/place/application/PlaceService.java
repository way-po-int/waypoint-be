package waypoint.mvp.place.application;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.error.PlaceError;
import waypoint.mvp.place.infrastructure.persistence.PlaceRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaceService {

	private final PlaceRepository placeRepository;

	public Place getById(Long placeId) {
		return placeRepository.findById(placeId)
			.orElseThrow(() -> new BusinessException(PlaceError.PLACE_NOT_FOUND));
	}

	public Place getById(String placeId) {
		return placeRepository.findByExternalId(placeId)
			.orElseThrow(() -> new BusinessException(PlaceError.PLACE_NOT_FOUND));
	}

	public List<Place> getByIds(List<String> placeIds) {
		return placeRepository.findAllByExternalIdIn(placeIds);
	}

	public Optional<Place> getPlace(String googlePlaceId) {
		return placeRepository.findByDetailPlaceId(googlePlaceId);
	}

	@Transactional
	public Place createOrGetPlace(Place place) {
		try {
			return placeRepository.save(place);
		} catch (DataIntegrityViolationException e) {
			return placeRepository.findByDetailPlaceId(place.getDetail().getPlaceId())
				.orElseThrow(() -> new BusinessException(PlaceError.PLACE_NOT_FOUND));
		}
	}

	@Transactional
	public void cachePhotoUri(String googlePlaceId, String photoUri) {
		if (!StringUtils.hasText(googlePlaceId) || !StringUtils.hasText(photoUri)) {
			return;
		}

		placeRepository.findByDetailPlaceId(googlePlaceId).ifPresent(place -> {
			var detail = place.getDetail();
			if (detail == null) {
				return;
			}

			if (StringUtils.hasText(detail.getPhotoUri())) {
				return;
			}

			detail.updatePhotoUri(photoUri);
		});
	}
}
