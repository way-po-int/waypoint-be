package waypoint.mvp.plan.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.Block;

public interface BlockRepository extends JpaRepository<Block, Long> {

	// @Query("SELECT b FROM Block b LEFT JOIN FETCH b.place JOIN FETCH b.addedBy WHERE b.timeBlock.id IN :timeBlockIds AND b.selected = true")
	// List<Block> findAllByTimeBlockIds(@Param("timeBlockIds") List<Long> timeBlockIds);

	@Query("SELECT b FROM Block b "
		+ "LEFT JOIN FETCH b.place "
		+ "LEFT JOIN FETCH b.manualPlace "
		+ "JOIN FETCH b.addedBy "
		+ "WHERE b.timeBlock.id IN :timeBlockIds AND b.timeBlock.planDay.plan.id = :planId")
	List<Block> findAllByTimeBlockIds(@Param("planId") Long planId, @Param("timeBlockIds") List<Long> timeBlockIds);

	@Query("SELECT b FROM Block b "
		+ "JOIN FETCH b.timeBlock tb "
		+ "JOIN FETCH tb.planDay "
		+ "LEFT JOIN FETCH b.place "
		+ "LEFT JOIN FETCH b.manualPlace "
		+ "LEFT JOIN FETCH b.socialMedia "
		+ "WHERE b.externalId = :blockId AND tb.planDay.plan.id = :planId")
	Optional<Block> findByExternalId(@Param("planId") Long planId, @Param("blockId") String blockId);

}
