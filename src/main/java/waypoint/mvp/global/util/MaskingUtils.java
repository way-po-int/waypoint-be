package waypoint.mvp.global.util;

public final class MaskingUtils {

	private MaskingUtils() {
	}

	public static String maskEmail(String email) {
		if (email == null || email.isBlank()) {
			return email;
		}

		int at = email.indexOf('@');
		int dot = email.lastIndexOf('.');

		if (at <= 0 || dot <= at + 1) {
			return email;
		}

		String local = email.substring(0, at);
		String domain = email.substring(at + 1, dot);
		String tld = email.substring(dot);

		String visibleLocal = local.length() >= 2 ? local.substring(0, 2) : local;
		String maskedLocal = visibleLocal + "***";

		String maskedDomain = domain.substring(0, 1) + "****";

		return maskedLocal + "@" + maskedDomain + tld;
	}
}
