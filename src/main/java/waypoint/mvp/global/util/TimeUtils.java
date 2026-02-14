package waypoint.mvp.global.util;

import java.time.LocalTime;

public class TimeUtils {
	private TimeUtils() {
	}

	public static boolean isValidRange(
		LocalTime start,
		LocalTime end
	) {
		if (start == null || end == null) {
			return true;
		}

		return end.isAfter(start);
	}
}
