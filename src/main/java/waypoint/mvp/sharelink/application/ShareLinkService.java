package waypoint.mvp.sharelink.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserInfo;
import waypoint.mvp.collection.application.CollectionService;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.sharelink.domain.ShareLink;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;
import waypoint.mvp.sharelink.error.ShareLinkError;
import waypoint.mvp.sharelink.infrastructure.ShareLinkRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ShareLinkService {

	private final ShareLinkRepository shareLinkRepository;
	private final CollectionService collectionService;

	@Value("${waypoint.frontend.base-url}")
	private String frontendBaseUrl;

	public sealed interface InvitationResult {
		String redirectUrl();

		record UserInvitation(String redirectUrl) implements InvitationResult {
		}

		record GuestInvitation(String redirectUrl, String shareLinkCode) implements InvitationResult {
		}
	}

	public InvitationResult processInvitationLink(String code, UserInfo userInfo) {
		ShareLink shareLink = findValidLink(code);
		String redirectUrl = buildRedirectUrl(shareLink);

		if (userInfo != null) {
			acceptInvitationForTarget(shareLink, userInfo.id());
			return new InvitationResult.UserInvitation(redirectUrl);
		} else {
			return new InvitationResult.GuestInvitation(redirectUrl, code);
		}
	}

	public ShareLink findShareLinkByCode(String code) {
		return shareLinkRepository.findByCode(code)
			.orElseThrow(() -> new BusinessException(ShareLinkError.INVALID_INVITATION_LINK));
	}

	@Transactional
	public void acceptInvitation(String code, Long userId) {
		ShareLink shareLink = findValidLink(code);
		acceptInvitationForTarget(shareLink, userId);
	}

	public ShareLink findValidLink(String code) {
		ShareLink shareLink = findShareLinkByCode(code);

		if (shareLink.isExpired()) {
			throw new BusinessException(ShareLinkError.EXPIRED_INVITATION_LINK);
		}
		return shareLink;
	}

	private void acceptInvitationForTarget(ShareLink shareLink, Long userId) {
		switch (shareLink.getTargetType()) {
			case COLLECTION:
				collectionService.addMemberFromShareLink(shareLink, userId);
				break;
			default:
				throw new BusinessException(ShareLinkError.INVALID_INVITATION_LINK);
		}
	}

	private String buildRedirectUrl(ShareLink shareLink) {
		ShareLinkType shareLinkType = shareLink.getTargetType();

		String path = switch (shareLinkType) {
			case COLLECTION -> shareLinkType.getPath();
			default -> throw new BusinessException(ShareLinkError.INVALID_INVITATION_LINK);
		};
		return frontendBaseUrl + path + shareLink.getTargetId();
	}
}
