package waypoint.mvp.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum NotificationEventType {

	// ===== 팀 전체 알림 =====

	// 플랜 장소 관련
	PLACE_ADDED_TO_PLAN(Category.PLAN, ActionType.PLACE_ADDED, false),
	PLACE_REMOVED_FROM_PLAN(Category.PLAN, ActionType.PLACE_REMOVED, false),

	// 컬렉션 장소 관련
	PLACE_ADDED_TO_COLLECTION(Category.COLLECTION, ActionType.PLACE_ADDED, false),
	PLACE_REMOVED_FROM_COLLECTION(Category.COLLECTION, ActionType.PLACE_REMOVED, false),

	// 예산 관련
	BUDGET_ITEM_ADDED(Category.PLAN, ActionType.BUDGET_ADDED, false),

	// ===== 개인 알림 =====

	// 컬렉션 활동 관련
	COLLECTION_MY_PLACE_PICKED(Category.COLLECTION, ActionType.MY_PLACE_PICKED, true),
	COLLECTION_PLACE_PASSED(Category.COLLECTION, ActionType.PLACE_PASSED, true),

	// 플랜 활동 관련
	PLAN_BLOCK_OPINION_ADDED(Category.PLAN, ActionType.OPINION_ADDED, true),
	PLAN_BLOCK_SELECTED(Category.PLAN, ActionType.BLOCK_SELECTED, true),

	// AI 관련
	AI_PLACE_EXTRACTION_SUCCESS(Category.SYSTEM, ActionType.AI_SUCCESS, true),
	AI_PLACE_EXTRACTION_FAILED(Category.SYSTEM, ActionType.AI_FAILED, true),

	// 시스템 알림
	SYSTEM_ANNOUNCEMENT(Category.SYSTEM, ActionType.ANNOUNCEMENT, false),
	SYSTEM_MAINTENANCE(Category.SYSTEM, ActionType.MAINTENANCE, true);

	private final Category category;
	private final ActionType actionType;
	private final boolean personalOnly;

	NotificationEventType(Category category, ActionType actionType, boolean personalOnly) {
		this.category = category;
		this.actionType = actionType;
		this.personalOnly = personalOnly;
	}

	/**
	 * 알림 메시지 자동 생성
	 */
	public String buildMessage(String actorNickname, String resourceTitle, String targetName) {
		return actionType.buildMessage(category, actorNickname, resourceTitle, targetName);
	}

	@Getter
	@RequiredArgsConstructor
	public enum Category {
		PLAN("플래너"),
		COLLECTION("보관함"),
		SYSTEM("시스템");

		private final String displayName;
	}

	@Getter
	@RequiredArgsConstructor
	public enum ActionType {
		PLACE_ADDED("추가했어요") {
			@Override
			public String buildMessage(Category category, String actorNickname, String resourceTitle, String targetName) {
				return String.format("%s님이 %s %s에 '%s'을(를) 추가했어요",
					actorNickname, category.getDisplayName(), resourceTitle, targetName);
			}
		},
		PLACE_REMOVED("제거했어요") {
			@Override
			public String buildMessage(Category category, String actorNickname, String resourceTitle, String targetName) {
				return String.format("%s님이 %s %s에서 '%s'을(를) 제거했어요",
					actorNickname, category.getDisplayName(), resourceTitle, targetName);
			}
		},
		BUDGET_ADDED("예산 추가") {
			@Override
			public String buildMessage(Category category, String actorNickname, String resourceTitle, String targetName) {
				return String.format("%s님이 %s %s에 %s원 지출을 추가했어요",
					actorNickname, category.getDisplayName(), resourceTitle, targetName);
			}
		},
		MY_PLACE_PICKED("찜") {
			@Override
			public String buildMessage(Category category, String actorNickname, String resourceTitle, String targetName) {
				return String.format("%s님이 내가 추가한 '%s'을(를) 찜했어요",
					actorNickname, targetName);
			}
		},
		PLACE_PASSED("패스") {
			@Override
			public String buildMessage(Category category, String actorNickname, String resourceTitle, String targetName) {
				return String.format("%s님이 '%s'을(를) 패스했어요",
					actorNickname, targetName);
			}
		},
		OPINION_ADDED("의견 추가") {
			@Override
			public String buildMessage(Category category, String actorNickname, String resourceTitle, String targetName) {
				return String.format("%s님이 '%s'에 의견을 남겼어요",
					actorNickname, targetName);
			}
		},
		BLOCK_SELECTED("선택됨") {
			@Override
			public String buildMessage(Category category, String actorNickname, String resourceTitle, String targetName) {
				return String.format("내가 추가한 '%s'이(가) 플랜에 선택되었어요",
					targetName);
			}
		},
		AI_SUCCESS("AI 성공") {
			@Override
			public String buildMessage(Category category, String actorNickname, String resourceTitle, String targetName) {
				return String.format("AI 장소 추출이 완료되었어요. %s개의 장소를 확인해보세요!",
					targetName);
			}
		},
		AI_FAILED("AI 실패") {
			@Override
			public String buildMessage(Category category, String actorNickname, String resourceTitle, String targetName) {
				return "AI 장소 추출에 실패했어요. 잠시 후 다시 시도해주세요";
			}
		},
		ANNOUNCEMENT("공지") {
			@Override
			public String buildMessage(Category category, String actorNickname, String resourceTitle, String targetName) {
				return String.format("[공지] %s", targetName);
			}
		},
		MAINTENANCE("점검") {
			@Override
			public String buildMessage(Category category, String actorNickname, String resourceTitle, String targetName) {
				return String.format("[점검] %s", targetName);
			}
		};

		private final String displayName;

		public abstract String buildMessage(Category category, String actorNickname, String resourceTitle, String targetName);
	}
}
