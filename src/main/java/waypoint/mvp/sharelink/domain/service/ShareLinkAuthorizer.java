package waypoint.mvp.sharelink.domain.service;

import java.time.Instant;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;
import waypoint.mvp.sharelink.infrastructure.ShareLinkRepository;

@Component
@RequiredArgsConstructor
public class ShareLinkAuthorizer {

	private final ShareLinkRepository shareLinkRepository;

	public void verifyAccess(Long targetId, String shareLinkCode, ShareLinkType type) {
		boolean valid = shareLinkRepository
			.existsValidShareLink(
				shareLinkCode,
				type,
				targetId,
				Instant.now()
			);

		if (!valid) {
			throw new BusinessException(CollectionError.FORBIDDEN_NOT_GUEST);
		}
	}

}
