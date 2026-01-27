package waypoint.mvp.global.common;

import java.time.Instant;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class LogicalDeleteEntity extends BaseTimeEntity {

	private Instant deletedAt;

	protected void softDelete() {
		this.deletedAt = Instant.now();
	}

	public void restore() {
		this.deletedAt = null;
	}
}
