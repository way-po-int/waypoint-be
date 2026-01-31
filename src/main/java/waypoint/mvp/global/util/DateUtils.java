package waypoint.mvp.global.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class DateUtils {

	private static final int MAX_PLAN_PERIOD_DAYS = 30;

	private DateUtils() {
	}

	public static boolean isNotBefore(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null) {
			return true;
		}
		return !endDate.isBefore(startDate);
	}

	public static boolean isWithinMaxPlanPeriod(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null) {
			return true;
		}
		return ChronoUnit.DAYS.between(startDate, endDate) <= MAX_PLAN_PERIOD_DAYS;
	}
}
