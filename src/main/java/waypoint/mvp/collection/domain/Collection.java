package waypoint.mvp.collection.domain;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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
import waypoint.mvp.global.common.LogicalDeleteEntity;

@Entity
@Table(name = "collections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE collections SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Collection extends LogicalDeleteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String thumbnail;

	@Column(nullable = false)
	private int memberCount = 0;

	@Builder(access = AccessLevel.PRIVATE)
	private Collection(String title, int memberCount) {
		this.title = title;
		this.thumbnail = "";
		this.memberCount = memberCount;
	}

	public static Collection create(String title) {
		return builder()
			.title(title)
			.memberCount(1)
			.build();
	}

	public void update(String title) {
		this.title = title;
	}

	public void updateThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}



	public void increaseMemberCount() {
		this.memberCount++;
	}

	public void decreaseMemberCount() {
		this.memberCount--;
	}
}
