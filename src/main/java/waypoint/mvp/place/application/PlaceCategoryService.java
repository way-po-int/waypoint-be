package waypoint.mvp.place.application;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.place.application.dto.PlaceCategoryDto;
import waypoint.mvp.place.application.dto.PlaceCategoryResponse;
import waypoint.mvp.place.domain.PlaceCategory;
import waypoint.mvp.place.domain.PlaceCategoryMapping;
import waypoint.mvp.place.infrastructure.persistence.PlaceCategoryMappingRepository;
import waypoint.mvp.place.infrastructure.persistence.PlaceCategoryRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceCategoryService {

	private static final Long ETC_CATEGORY_ID = 905099L;

	private final PlaceCategoryRepository categoryRepository;
	private final PlaceCategoryMappingRepository mappingRepository;

	private Map<String, Long> typeToIdMap;
	private Map<Long, PlaceCategoryDto> idToCategoryMap;

	@PostConstruct
	private void init() {
		loadCategory();
	}

	public void loadCategory() {
		this.typeToIdMap = mappingRepository.findAll()
			.stream()
			.collect(Collectors.toUnmodifiableMap(
				PlaceCategoryMapping::getPrimaryType,
				m -> m.getCategory().getId()));

		this.idToCategoryMap = categoryRepository.findAll()
			.stream()
			.collect(Collectors.toUnmodifiableMap(PlaceCategory::getId, PlaceCategoryDto::from));

		log.info("카테고리 데이터 로드 완료 - primaryType {}개, 카테고리 {}개",
			typeToIdMap.size(), idToCategoryMap.size());
	}

	public Long getCategoryId(String primaryType) {
		if (primaryType == null) {
			return ETC_CATEGORY_ID;
		}
		return typeToIdMap.getOrDefault(primaryType, ETC_CATEGORY_ID);
	}

	public PlaceCategoryResponse toCategoryResponse(Long categoryId) {
		PlaceCategoryDto leaf = idToCategoryMap.getOrDefault(categoryId, idToCategoryMap.get(ETC_CATEGORY_ID));
		if (ObjectUtils.isEmpty(leaf)) {
			log.error("기타(ETC) 카테고리(ID: {})가 존재하지 않습니다. 데이터 초기화 로직을 확인해주세요.", ETC_CATEGORY_ID);
			return new PlaceCategoryResponse(null, null, null);
		}

		PlaceCategoryDto[] byLevel = new PlaceCategoryDto[4];

		leaf.pathIds()
			.stream()
			.map(idToCategoryMap::get)
			.filter(Objects::nonNull)
			.forEach(c -> byLevel[c.level()] = c);

		return new PlaceCategoryResponse(byLevel[1], byLevel[2], byLevel[3]);
	}
}
