package waypoint.mvp.plan.application.dto;

import static waypoint.mvp.plan.application.dto.BlockCreateCommandMessages.*;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.plan.application.dto.request.BlockCreateByPlaceRequest;
import waypoint.mvp.plan.application.dto.request.BlockCreateRequest;
import waypoint.mvp.plan.domain.TimeBlockType;
import waypoint.mvp.plan.error.BlockError;

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

	public BlockCreateCommand {
		Assert.notNull(blockType, BLOCK_TYPE_REQUIRED);
		Assert.notNull(createType, CREATE_TYPE_REQUIRED);
		Assert.notNull(day, DAY_REQUIRED);
		Assert.notNull(startTime, START_TIME_REQUIRED);
		Assert.notNull(endTime, END_TIME_REQUIRED);

		Assert.isTrue(day >= 1, DAY_MIN_VALUE);
		Assert.isTrue(startTime.isBefore(endTime), START_TIME_BEFORE_END_TIME);

		validateBlockConstraints(blockType, createType, collectionPlaceId, placeId);
	}

	private void validateBlockConstraints(TimeBlockType type, CreateType createType, String collectionPlaceId,
		String placeId) {
		if (type == TimeBlockType.FREE) {
			Assert.isNull(collectionPlaceId, FREE_BLOCK_NO_COLLECTION_PLACE);
			Assert.isNull(placeId, FREE_BLOCK_NO_PLACE);
			return;
		}

		// PLACE 타입인 경우의 상세 검증
		switch (createType) {
			case COLLECT_PLACE -> {
				Assert.hasText(collectionPlaceId, COLLECT_PLACE_REQUIRES_COLLECTION_PLACE_ID);
				Assert.isNull(placeId, COLLECT_PLACE_NO_PLACE_ID);
			}
			case PLACE -> {
				Assert.hasText(placeId, PLACE_REQUIRES_PLACE_ID);
				Assert.isNull(collectionPlaceId, PLACE_NO_COLLECTION_PLACE_ID);
			}
			case MANUAL -> {
				Assert.hasText(placeId, MANUAL_NOT_SUPPORTED);
			}

			default -> throw new BusinessException(
				BlockError.BLOCK_CREATE_TYPE_UNSUPPORTED,
				Arrays.stream(BlockCreateCommand.CreateType.values())
					.map(Enum::name)
					.collect(Collectors.joining(", "))
			);

		}
	}

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
			TimeBlockType.PLACE,
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
