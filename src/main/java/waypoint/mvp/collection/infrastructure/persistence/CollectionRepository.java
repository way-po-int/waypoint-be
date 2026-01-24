package waypoint.mvp.collection.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import waypoint.mvp.collection.domain.Collection;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

	@Query("SELECT cm.collection FROM CollectionMember cm WHERE cm.user.id = :userId")
	Page<Collection> findAllByUserId(Long userId, Pageable pageable);

}
