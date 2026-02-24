package waypoint.mvp.plan.application.dto.request;

import java.time.LocalTime;

import org.hibernate.validator.constraints.Range;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import waypoint.mvp.global.util.TimeUtils;
import waypoint.mvp.global.validation.annotation.MemoPolicy;
import waypoint.mvp.place.application.dto.ManualPlaceInfo;
import waypoint.mvp.plan.application.dto.BlockCreateCommand;

public record BlockManualCreateRequest(
	@NotNull(message = "일차는 필수입니다.")
	@Range(min = 1, max = 30, message = "일차는 1~30 사이여야 합니다.")
	Integer day,

	@NotNull(message = "시작 시간은 필수입니다.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime startTime,

	@NotNull(message = "종료 시간은 필수입니다.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime endTime,

	@Size(max = 300, message = "memo는 최대 300자까지 입력할 수 있습니다.")
	@MemoPolicy
	String memo,

	@NotNull(message = "장소 정보는 필수입니다.")
	@Valid
	ManualPlaceInfo manualPlaceInfo
) {

	@JsonIgnore
	@AssertTrue(message = "종료 시간은 시작 시간보다 이후여야 합니다.")
	public boolean isTimeRangeValid() {
		return TimeUtils.isValidRange(startTime, endTime);
	}

	public BlockCreateCommand toCommand() {
		return BlockCreateCommand.fromManual(this);
	}
}
