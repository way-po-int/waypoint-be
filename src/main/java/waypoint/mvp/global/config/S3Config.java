package waypoint.mvp.global.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

	@Bean
	public S3Presigner s3Presigner(
		@Value("${aws.region}") String region
	) {
		return S3Presigner.builder()
			.region(Region.of(region))
			.build();
	}

	@Bean
	public S3Client s3Client(
		@Value("${aws.region}") String region
	) {
		return S3Client.builder()
			.region(Region.of(region))
			.overrideConfiguration(ClientOverrideConfiguration.builder()
				.apiCallTimeout(Duration.ofSeconds(60))
				.apiCallAttemptTimeout(Duration.ofSeconds(20))
				.build())
			.build();
	}
}
