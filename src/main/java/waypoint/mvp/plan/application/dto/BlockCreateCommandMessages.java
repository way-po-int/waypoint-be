package waypoint.mvp.plan.application.dto;

public final class BlockCreateCommandMessages {

	private BlockCreateCommandMessages() {
	}

	private static final String REQUIRED_TEMPLATE = "%s은(는) 필수입니다.";
	private static final String CANNOT_HAVE_TEMPLATE = "%s은(는) %s을(를) 가질 수 없습니다.";
	private static final String REQUIRES_TEMPLATE = "%s 타입은 %s이(가) 필수입니다.";

	public static final String BLOCK_TYPE_REQUIRED = required("blockType");
	public static final String CREATE_TYPE_REQUIRED = required("createType");
	public static final String DAY_REQUIRED = required("day");
	public static final String START_TIME_REQUIRED = required("startTime");
	public static final String END_TIME_REQUIRED = required("endTime");

	public static final String DAY_MIN_VALUE = "day는 1 이상이어야 합니다.";
	public static final String START_TIME_BEFORE_END_TIME = "startTime은 endTime보다 이전이어야 합니다.";

	public static final String FREE_BLOCK_NO_COLLECTION_PLACE = cannotHave("FREE 블록", "collectionPlaceId");
	public static final String FREE_BLOCK_NO_PLACE = cannotHave("FREE 블록", "placeId");

	public static final String COLLECT_PLACE_REQUIRES_COLLECTION_PLACE_ID = requires("COLLECT_PLACE", "collectionPlaceId");
	public static final String COLLECT_PLACE_NO_PLACE_ID = cannotHave("COLLECT_PLACE 타입", "placeId");

	public static final String PLACE_REQUIRES_PLACE_ID = requires("PLACE", "placeId");
	public static final String PLACE_NO_COLLECTION_PLACE_ID = cannotHave("PLACE 타입", "collectionPlaceId");

	public static final String MANUAL_NOT_SUPPORTED = "MANUAL 타입은 현재 지원하지 않습니다.";

	private static String required(String fieldName) {
		return String.format(REQUIRED_TEMPLATE, fieldName);
	}

	private static String cannotHave(String subject, String object) {
		return String.format(CANNOT_HAVE_TEMPLATE, subject, object);
	}

	private static String requires(String type, String fieldName) {
		return String.format(REQUIRES_TEMPLATE, type, fieldName);
	}
}
