package waypoint.mvp.sharelink.application.dto.response;

import waypoint.mvp.sharelink.domain.ShareLink;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;

public record ShareLinkResponse(
	ShareLinkType type,
	String referenceId,
	String url,
	String ttl
) {

	public static ShareLinkResponse from(ShareLink shareLink, String url) {
		return new ShareLinkResponse(
			shareLink.getTargetType(),
			shareLink.getTargetExternalId(),
			url,
			shareLink.getExpiresAt().toString()
		);
	}
}
