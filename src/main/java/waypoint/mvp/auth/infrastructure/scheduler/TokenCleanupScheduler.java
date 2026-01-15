package waypoint.mvp.auth.infrastructure.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.auth.application.AuthService;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

	private final AuthService authService;

	@Scheduled(cron = "0 0 4 * * *")
	public void run() {
		log.info("리프레시 토큰 삭제 스케줄러 시작");
		long deletedCount = authService.deleteExpiredTokens();
		log.info("만료된 리프레시 토큰 삭제 완료: deletedCount={}", deletedCount);
	}
}
