package waypoint.mvp.collection.application.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.collection.application.CollectionMemberService;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.event.CollectionCreatedEvent;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.user.domain.User;
import waypoint.mvp.user.domain.event.ProfileUpdateEvent;
import waypoint.mvp.user.error.UserError;
import waypoint.mvp.user.infrastructure.persistence.UserRepository;

@Component
@RequiredArgsConstructor
public class CollectionEventListener {

	private final CollectionRepository collectionRepository;
	private final UserRepository userRepository;
	private final CollectionMemberService collectionMemberService;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleCollectionCreatedEvent(CollectionCreatedEvent event) {
		Collection collection = collectionRepository.findById(event.collectionId())
			.orElseThrow(() -> new BusinessException(CollectionError.COLLECTION_NOT_FOUND));

		User user = userRepository.findById(event.user().id())
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		collectionMemberService.createInitialOwner(collection, user);
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleProfileUpdateEvent(ProfileUpdateEvent event) {
		collectionMemberService.updateMemberProfile(event.userId(), event.nickname(), event.picture());
	}
}
