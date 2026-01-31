package waypoint.mvp.global.common;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class ExternalIdEntity extends BaseTimeEntity {

	@Column(unique = true, nullable = false, updatable = false, length = 21)
	private String externalId;

	@PrePersist
	protected void onCreate() {
		if (this.externalId == null) {
			this.externalId = NanoIdUtils.randomNanoId();
		}
	}

}
