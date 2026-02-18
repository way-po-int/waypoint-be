package waypoint.mvp.plan.application.dto;

import java.time.LocalTime;

import waypoint.mvp.plan.application.dto.request.BlockCreateByPlaceRequest;
import waypoint.mvp.plan.application.dto.request.BlockCreateRequest;
import waypoint.mvp.plan.domain.TimeBlockType;

/**
 * Block 생성을 위한 내부 커맨드 객체
 * 외부 Request를 이 객체로 변환하여 서비스 로직 통합
 */
public record BlockCreateCommand(
	TimeBlockType blockType,
	CreateType createType,
	Integer day,
	LocalTime startTime,
	LocalTime endTime,
	String memo,
	String collectionPlaceId,
	String placeId
) {

	public enum CreateType {
		COLLECT_PLACE, // 컬렉션에서 선택한 경우
		PLACE,  // 장소 검색에서 선택한 경우
		MANUAL // 사용자가 수동으로 장소 입력한 경우
	}

	public static BlockCreateCommand from(BlockCreateRequest request) {
		return new BlockCreateCommand(
			request.type(),
			CreateType.COLLECT_PLACE,
			request.day(),
			request.startTime(),
			request.endTime(),
			request.memo(),
			request.collectionPlaceId(),
			null
		);
	}

	public static BlockCreateCommand from(BlockCreateByPlaceRequest request) {
		return new BlockCreateCommand(
			request.type(),
			CreateType.PLACE,
			request.day(),
			request.startTime(),
			request.endTime(),
			request.memo(),
			null,
			request.placeId()
		);
	}

	public boolean isPlaceBlock() {
		return blockType == TimeBlockType.PLACE;
	}

}
