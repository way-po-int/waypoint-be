package waypoint.mvp.place.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExtractFailureCode {
	// 사용자 에러
	CONTENT_NOT_FOUND("콘텐츠를 찾을 수 없습니다.", false),
	NO_PLACE_EXTRACTED("콘텐츠를 분석했으나 장소를 찾을 수 없습니다.", false),
	VIDEO_TOO_SHORT("분석할 수 없는 영상입니다. (라이브 또는 길이 정보 없음)", false),
	VIDEO_TOO_LONG("영상 길이가 제한을 초과했습니다.", false),

	// 시스템 에러
	UNEXPECTED_ERROR("예기치 못한 오류가 발생했습니다.", false),
	YOUTUBE_API_ERROR("YouTube API 에러 발생", true),
	GENAI_ERROR("GenAI 에러 발생", true);

	private final String message;
	private final boolean isRetryable;
}
