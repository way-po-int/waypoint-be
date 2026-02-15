package waypoint.mvp.plan.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.BlockOpinion;

public interface BlockOpinionRepository extends JpaRepository<BlockOpinion, Long> {

	@Query("SELECT bo FROM BlockOpinion bo "
		+ "JOIN FETCH bo.addedBy "
		+ "WHERE bo.block.id IN :blockIds")
	List<BlockOpinion> findAllByBlockIds(@Param("blockIds") List<Long> blockIds);
}
