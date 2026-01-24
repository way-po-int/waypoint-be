package waypoint.mvp.global.auth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API의 인가 정책을 지정하는 통합 어노테이션입니다.
 * AOP가 이 어노테이션을 감지하여 {@code AuthLevel}에 맞는 검증 로직을 수행합니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorize {
	AuthLevel level();
}
