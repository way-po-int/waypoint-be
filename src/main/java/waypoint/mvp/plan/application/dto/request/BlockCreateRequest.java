package waypoint.mvp.plan.application.dto.request;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import waypoint.mvp.global.validation.annotation.MemoPolicy;
import waypoint.mvp.plan.domain.TimeBlockType;

public record BlockCreateRequest(

	@NotNull(message = "블록 타입은 필수입니다.")
	TimeBlockType type,

	String collectionPlaceId,

	@NotNull(message = "일차는 필수입니다.")
	@Positive(message = "일차는 1 이상이어야 합니다.")
	Integer day,

	@NotNull(message = "시작 시간은 필수입니다.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime startTime,

	@NotNull(message = "종료 시간은 필수입니다.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime endTime,

	@Size(max = 300, message = "memo는 최대 300자까지 입력할 수 있습니다.")
	@MemoPolicy
	String memo
) {

	@JsonIgnore
	@AssertTrue(message = "장소 블록에는 collection_place_id가 필수입니다.")
	public boolean isCollectionPlaceIdValid() {
		if (type == TimeBlockType.PLACE) {
			return collectionPlaceId != null && !collectionPlaceId.isBlank();
		}
		return true;
	}

	@JsonIgnore
	@AssertTrue(message = "종료 시간은 시작 시간보다 이후여야 합니다.")
	public boolean isTimeRangeValid() {
		if (startTime == null || endTime == null)
			return true; // @NotNull에서 걸러짐
		return endTime.isAfter(startTime);
	}
}
