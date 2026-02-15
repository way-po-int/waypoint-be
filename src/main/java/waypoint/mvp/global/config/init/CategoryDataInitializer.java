package waypoint.mvp.global.config.init;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.place.application.PlaceCategoryService;
import waypoint.mvp.place.infrastructure.persistence.PlaceCategoryRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryDataInitializer implements CommandLineRunner {

	private static final String[] SQL_FILES = {
		"sql/place-category.sql",
		"sql/place-category-mapping.sql"
	};

	private final PlaceCategoryService placeCategoryService;
	private final PlaceCategoryRepository placeCategoryRepository;
	private final DataSource dataSource;

	@Override
	public void run(String... args) {
		long count = placeCategoryRepository.count();
		if (count > 0) {
			log.info("카테고리 데이터가 이미 존재합니다. (총 {}개)", count);
			return;
		}

		log.info("카테고리 데이터 초기화를 시작합니다.");

		try (Connection connection = dataSource.getConnection()) {
			for (String fileName : SQL_FILES) {
				log.info("SQL 파일 실행 중: {}", fileName);
				ClassPathResource sqlFile = new ClassPathResource(fileName);
				ScriptUtils.executeSqlScript(connection, sqlFile);
			}

			log.info("카테고리 데이터 초기화 완료");
			placeCategoryService.loadCategory();

		} catch (SQLException e) {
			log.error("카테고리 데이터 초기화에 실패했습니다.", e);
		}
	}
}
