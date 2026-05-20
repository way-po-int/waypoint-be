package waypoint.mvp.global.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

	public static final String COLLECTION_MEMBERS_CACHE = "collectionMembers";
	public static final String PLAN_MEMBERS_CACHE = "planMembers";

	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager(
			COLLECTION_MEMBERS_CACHE,
			PLAN_MEMBERS_CACHE
		);

		cacheManager.setCaffeine(Caffeine.newBuilder()
			.expireAfterWrite(50, TimeUnit.MINUTES)
			.maximumSize(10000)
		);

		return cacheManager;
	}
}
