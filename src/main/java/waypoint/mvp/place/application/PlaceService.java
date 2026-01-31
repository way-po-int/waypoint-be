package waypoint.mvp.place.application;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	public Set<String> findExistingPlaceIds(Set<String> placeIds) {
		return placeRepository.findExistingPlaceIds(placeIds);
	}
}
