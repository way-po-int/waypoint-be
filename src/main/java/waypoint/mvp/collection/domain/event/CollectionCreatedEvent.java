package waypoint.mvp.collection.domain.event;

import waypoint.mvp.auth.security.principal.UserInfo;

public record CollectionCreatedEvent(
	Long collectionId,
	UserInfo user
) {
	public static CollectionCreatedEvent of(Long collectionId, UserInfo user) {
		return new CollectionCreatedEvent(collectionId, user);
	}
}
