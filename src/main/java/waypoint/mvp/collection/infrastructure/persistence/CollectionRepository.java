package waypoint.mvp.collection.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.collection.domain.Collection;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

	@Query("SELECT c FROM Collection c WHERE c.id IN " +
		"(SELECT cm.collection.id FROM CollectionMember cm WHERE cm.user.id = :userId)")
	Slice<Collection> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

	Optional<Collection> findByExternalId(String externalId);
}
