package waypoint.mvp.plan.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BlockStatus {
	FIXED,   //장소 확정
	PENDING, //후보지 존재, 미확정
	DIRECT;  //후보지 없음
}
