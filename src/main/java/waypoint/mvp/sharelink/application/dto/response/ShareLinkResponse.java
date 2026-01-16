package waypoint.mvp.sharelink.application.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import waypoint.mvp.sharelink.domain.ShareLink;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;

public record ShareLinkResponse(
	ShareLinkType type,
	Long referenceId,
	Long hostId,
	String code,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	Instant ttl
) {
	public static ShareLinkResponse from(ShareLink shareLink) {
		return new ShareLinkResponse(
			shareLink.getTargetType(),
			shareLink.getTargetId(),
			shareLink.getHostUserId(),
			shareLink.getCode(),
			shareLink.getExpiresAt()
		);
	}
}
