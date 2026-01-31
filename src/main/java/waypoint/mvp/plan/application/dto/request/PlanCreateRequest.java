package waypoint.mvp.plan.application.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import waypoint.mvp.global.util.DateUtils;

public record PlanCreateRequest(

	@NotBlank(message = "제목은 비어있을 수 없습니다.")
	@Size(min = 1, max = 20, message = "제목은 1자 이상 20자 이하로 입력해주세요.")
	String title,

	@NotNull(message = "시작일은 필수입니다.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	LocalDate startDate,

	@NotNull(message = "종료일은 필수입니다.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	LocalDate endDate

) {
	@AssertTrue(message = "종료일이 시작일보다 빠를 수 없습니다.")
	public boolean isValidPeriod() {
		return DateUtils.isNotBefore(startDate, endDate);
	}

	@AssertTrue(message = "여행 기간은 최대 30일입니다.")
	public boolean isWithinMaxPeriod() {
		return DateUtils.isWithinMaxPlanPeriod(startDate, endDate);
	}
}
