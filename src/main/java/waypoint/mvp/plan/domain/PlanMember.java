package waypoint.mvp.plan.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.LogicalDeleteEntity;
import waypoint.mvp.global.common.Membership;
import waypoint.mvp.user.domain.User;

@Entity
@Table(name = "plan_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanMember extends LogicalDeleteEntity implements Membership {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Plan plan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private User user;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false)
	private String picture;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PlanRole role;

	@Builder(access = AccessLevel.PRIVATE)
	private PlanMember(Plan plan, User user, String nickname, String picture, PlanRole role) {
		this.plan = plan;
		this.user = user;
		this.nickname = nickname;
		this.picture = picture;
		this.role = role;
	}

	public static PlanMember create(Plan plan, User user, PlanRole role) {
		return builder()
			.plan(plan)
			.user(user)
			.nickname(user.getNickname())
			.picture(user.getPicture())
			.role(role)
			.build();
	}

	public boolean isOwner() {
		return this.role == PlanRole.OWNER;
	}

	public void withdraw() {
		super.softDelete();
	}

	public void rejoin() {
		super.restore();
	}

	public void updateProfile(String nickname, String picture) {
		this.nickname = nickname;
		this.picture = picture;
	}

	public void updateRole(PlanRole role) {
		this.role = role;
	}
}
