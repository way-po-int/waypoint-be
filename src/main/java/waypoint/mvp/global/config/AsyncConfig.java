package waypoint.mvp.global.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean(name = "placeExtractionTaskExecutor")
	public Executor placeExtractionTaskExecutor() {
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("vt-place-extraction-");
		executor.setVirtualThreads(true);
		return executor;
	}

	@Bean(name = "placeSearchTaskExecutor")
	public Executor placeSearchTaskExecutor() {
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("vt-place-search-");
		executor.setVirtualThreads(true);
		return executor;
	}

	@Bean(name = "notificationTaskExecutor")
	public Executor notificationTaskExecutor() {
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("vt-notification-");
		executor.setVirtualThreads(true);
		return executor;
	}
}
