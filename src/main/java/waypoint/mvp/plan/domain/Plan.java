package waypoint.mvp.plan.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import waypoint.mvp.global.common.BaseTimeEntity;

@Entity
@Table(name = "plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false)
	private int memberCount = 0;

	@Builder(access = AccessLevel.PRIVATE)
	private Plan(String title, LocalDate startDate, LocalDate endDate, int memberCount) {
		this.title = title;
		this.startDate = startDate;
		this.endDate = endDate;
		this.memberCount = memberCount;
	}

	public static Plan create(String title, LocalDate startDate, LocalDate endDate) {
		return builder()
			.title(title)
			.startDate(startDate)
			.endDate(endDate)
			.build();
	}
}
