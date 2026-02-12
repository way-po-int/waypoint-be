package waypoint.mvp.plan.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.TimeBlock;

public interface BlockRepository extends JpaRepository<Block, Long> {

	void deleteAllByTimeBlockIn(List<TimeBlock> timeBlocks);
}
