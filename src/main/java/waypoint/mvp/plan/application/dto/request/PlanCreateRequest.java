package waypoint.mvp.plan.application.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlanCreateRequest(

	@NotBlank(message = "제목은 비어있을 수 없습니다.")
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
		if (startDate == null || endDate == null) {
			return true; // Null 체크는 @NotNull 담당이므로 여기선 통과시켜야 중복 에러가 안 나
		}
		return !endDate.isBefore(startDate);
	}
}
