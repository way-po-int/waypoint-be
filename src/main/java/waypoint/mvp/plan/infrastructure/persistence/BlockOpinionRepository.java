package waypoint.mvp.plan.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.BlockOpinion;

public interface BlockOpinionRepository extends JpaRepository<BlockOpinion, Long> {

	@Query("SELECT bo FROM BlockOpinion bo "
		+ "JOIN FETCH bo.addedBy "
		+ "LEFT JOIN FETCH bo.opinionTagIds "
		+ "WHERE bo.block.id IN :blockIds")
	List<BlockOpinion> findAllByBlockIds(@Param("blockIds") List<Long> blockIds);

	@Query("SELECT bo FROM BlockOpinion bo"
		+ " JOIN FETCH bo.addedBy LEFT JOIN FETCH bo.opinionTagIds"
		+ " WHERE bo.block.id = :blockId ORDER BY bo.id ASC")
	List<BlockOpinion> findAllByBlockId(@Param("blockId") Long blockId);

	@Query("SELECT bo FROM BlockOpinion bo"
		+ " JOIN FETCH bo.addedBy LEFT JOIN FETCH bo.opinionTagIds"
		+ " WHERE bo.block.id = :blockId AND bo.externalId = :externalId")
	Optional<BlockOpinion> findByBlockIdAndExternalId(
		@Param("blockId") Long blockId, @Param("externalId") String externalId);

	boolean existsByBlockIdAndAddedById(Long blockId, Long addedById);
}
