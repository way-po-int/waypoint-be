package waypoint.mvp.sharelink.application.dto.response;

import waypoint.mvp.sharelink.domain.ShareLink;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;

public record ShareLinkResponse(
	ShareLinkType type,
	String referenceId,
	String code,
	String ttl
) {
	public static ShareLinkResponse from(ShareLink shareLink) {
		return new ShareLinkResponse(
			shareLink.getTargetType(),
			shareLink.getTargetExternalId(),
			shareLink.getCode(),
			shareLink.getExpiresAt().toString()
		);
	}
}
