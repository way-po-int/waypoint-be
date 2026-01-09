package waypoint.mvp.auth.infrastructure.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.application.AuthService;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

	private final AuthService authService;

	@Scheduled(cron = "0 0 4 * * *")
	public void run() {
		authService.deleteExpiredTokens();
	}
}
