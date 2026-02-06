package waypoint.mvp.collection.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CollectionCreateRequest(
	@NotBlank(message = "컬렉션 제목은 비워둘 수 없습니다.")
	@Size(max = 20, message = "컬렉션 제목은 20자를 초과할 수 없습니다.")
	@Pattern(
		// \p{L}: 모든 언어 문자 (한글, 영문 등)
		// \p{N}: 모든 숫자
		// ' _ : 기획에서 허용하기로 한 특수문자 및 공백
		// \p{So}\p{Sk}\p{Sm}\p{Sc}: 이모지 및 기호류 카테고리
		// \p{Cs}: Surrogate pair (이모지 조합용 필수)
		regexp = "^[\\p{L}\\p{N}' _\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}\\p{Cs}]*$",
		message = "한글, 영문, 숫자, 특수문자(' _), 이모지만 사용할 수 있습니다."
	)
	String title
) {
}
