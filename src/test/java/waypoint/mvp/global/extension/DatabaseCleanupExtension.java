package waypoint.mvp.global.extension;

import java.util.List;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class DatabaseCleanupExtension implements AfterEachCallback {

	@Override
	public void afterEach(ExtensionContext context) {
		ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
		JdbcTemplate jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);

		List<String> tableNames = findTableNames(jdbcTemplate);
		truncateTables(jdbcTemplate, tableNames);
	}

	private List<String> findTableNames(JdbcTemplate jdbcTemplate) {
		String query = "SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname = 'public'";
		return jdbcTemplate.queryForList(query, String.class)
			.stream()
			.filter(tableName -> !isSystemTable(tableName))
			.toList();
	}

	private boolean isSystemTable(String tableName) {
		return tableName.equals("spatial_ref_sys");
	}

	private void truncateTables(JdbcTemplate jdbcTemplate, List<String> tableNames) {
		for (String tableName : tableNames) {
			jdbcTemplate.execute("TRUNCATE TABLE " + tableName + " RESTART IDENTITY CASCADE");
		}
	}
}
