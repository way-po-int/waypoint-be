package waypoint.mvp.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import waypoint.mvp.global.config.TestContainersConfig;
import waypoint.mvp.global.extension.DatabaseCleanupExtension;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@ExtendWith(DatabaseCleanupExtension.class)
public @interface ServiceTest {
}
