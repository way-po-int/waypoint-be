package waypoint.mvp.plan.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.plan.domain.PlanMember;

public interface PlanMemberRepository extends JpaRepository<PlanMember, Long> {

}
