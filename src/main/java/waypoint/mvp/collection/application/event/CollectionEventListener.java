package waypoint.mvp.collection.application.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionRole;
import waypoint.mvp.collection.domain.event.CollectionCreatedEvent;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.user.domain.User;
import waypoint.mvp.user.infrastructure.persistence.UserRepository;

@Component
@RequiredArgsConstructor
public class CollectionEventListener {

	private final CollectionRepository collectionRepository;
	private final CollectionMemberRepository collectionMemberRepository;
	private final UserRepository userRepository;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleCollectionCreatedEvent(CollectionCreatedEvent event) {
		Collection collection = collectionRepository.findById(event.collectionId())
			.orElseThrow(() -> new IllegalArgumentException("Collection not found"));

		User user = userRepository.findById(event.user().id())
			.orElseThrow(() -> new IllegalArgumentException("User not found"));

		CollectionMember collectionMember = CollectionMember.create(collection, user, CollectionRole.OWNER);
		collectionMemberRepository.save(collectionMember);
	}
}
