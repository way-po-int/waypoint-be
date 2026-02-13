package waypoint.mvp.plan.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.Block;

public interface BlockRepository extends JpaRepository<Block, Long> {

	@Query("SELECT b FROM Block b JOIN FETCH b.place JOIN FETCH b.addedBy WHERE b.timeBlock.id IN :timeBlockIds")
	List<Block> findAllByTimeBlockIds(@Param("timeBlockIds") List<Long> timeBlockIds);

	@Query("SELECT b FROM Block b "
		+ "JOIN FETCH b.timeBlock tb "
		+ "JOIN FETCH tb.planDay "
		+ "JOIN FETCH b.place "
		+ "JOIN FETCH b.socialMedia "
		+ "WHERE b.externalId = :externalId")
	Optional<Block> findByExternalIdWithFetch(@Param("externalId") String externalId);
}
