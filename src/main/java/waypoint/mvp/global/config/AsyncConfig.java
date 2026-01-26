package waypoint.mvp.global.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean(name = "extractionTaskExecutor")
	public Executor extractionTaskExecutor() {
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("vt-extraction-");
		executor.setVirtualThreads(true);
		return executor;
	}
}
