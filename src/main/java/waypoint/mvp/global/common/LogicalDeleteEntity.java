package waypoint.mvp.global.common;

import java.time.Instant;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class LogicalDeleteEntity extends ExternalIdEntity {

	private Instant deletedAt;

	protected void softDelete() {
		this.deletedAt = Instant.now();
	}

	public boolean isDeleted() {
		return this.deletedAt != null;
	}

	public void restore() {
		this.deletedAt = null;
	}
}
