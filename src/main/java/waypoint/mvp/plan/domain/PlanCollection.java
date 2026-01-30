package waypoint.mvp.plan.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.global.common.ExternalIdEntity;

@Entity
@Table(
	name = "plan_collections",
	uniqueConstraints = @UniqueConstraint(columnNames = {"plan_id", "collection_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanCollection extends ExternalIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Plan plan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Collection collection;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private PlanMember member;

	@Builder(access = AccessLevel.PRIVATE)
	private PlanCollection(Plan plan, Collection collection, PlanMember member) {
		this.plan = plan;
		this.collection = collection;
		this.member = member;
	}

	public static PlanCollection create(Plan plan, Collection collection, PlanMember member) {
		return builder()
			.plan(plan)
			.collection(collection)
			.member(member)
			.build();
	}
}
