package waypoint.mvp.plan.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.plan.domain.Block;

public interface BlockRepository extends JpaRepository<Block, Long> {
}
