package waypoint.mvp.collection.domain.event;

import waypoint.mvp.auth.security.principal.UserPrincipal;

public record CollectionCreatedEvent(
	Long collectionId,
	UserPrincipal user
) {
	public static CollectionCreatedEvent of(Long collectionId, UserPrincipal user) {
		return new CollectionCreatedEvent(collectionId, user);
	}
}
