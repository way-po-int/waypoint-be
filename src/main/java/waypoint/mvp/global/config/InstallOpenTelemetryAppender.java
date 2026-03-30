package waypoint.mvp.global.config;

import org.springframework.stereotype.Component;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstallOpenTelemetryAppender {

	private final OpenTelemetry openTelemetry;

	@PostConstruct
	public void init() {
		OpenTelemetryAppender.install(this.openTelemetry);
	}
}
