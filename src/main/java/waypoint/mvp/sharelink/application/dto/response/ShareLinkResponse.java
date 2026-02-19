package waypoint.mvp.sharelink.application.dto.response;

import waypoint.mvp.sharelink.domain.ShareLink;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;

public record ShareLinkResponse(
	ShareLinkType type,
	String referenceId,
	String url,
	String ttl
) {
	private static final String INVITE_PATH_PREFIX = "/invite/";

	public static ShareLinkResponse from(ShareLink shareLink) {
		return new ShareLinkResponse(
			shareLink.getTargetType(),
			shareLink.getTargetExternalId(),
			INVITE_PATH_PREFIX + shareLink.getCode(),
			shareLink.getExpiresAt().toString()
		);
	}
}
