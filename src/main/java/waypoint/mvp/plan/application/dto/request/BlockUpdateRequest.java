package waypoint.mvp.plan.application.dto.request;

import java.time.LocalTime;

import org.hibernate.validator.constraints.Range;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import waypoint.mvp.global.util.TimeUtils;
import waypoint.mvp.global.validation.annotation.MemoPolicy;

public record BlockUpdateRequest(

	@Range(min = 1, max = 30, message = "일차는 1~30 사이여야 합니다.")
	Integer day,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime startTime,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	LocalTime endTime,

	@Size(max = 300, message = "memo는 최대 300자까지 입력할 수 있습니다.")
	@MemoPolicy
	String memo
) {

	@JsonIgnore
	@AssertTrue(message = "시작 시간과 종료 시간은 함께 입력해야 합니다.")
	public boolean isTimePairValid() {
		if (startTime == null && endTime == null) {
			return true;
		}
		return startTime != null && endTime != null;
	}

	@JsonIgnore
	@AssertTrue(message = "종료 시간은 시작 시간보다 이후여야 합니다.")
	public boolean isTimeRangeValid() {
		return TimeUtils
			.isValidRange(startTime, endTime);
	}
}
