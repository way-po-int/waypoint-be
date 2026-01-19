package waypoint.mvp.sharelink.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserInfo;
import waypoint.mvp.collection.application.CollectionService;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.sharelink.domain.ShareLink;
import waypoint.mvp.sharelink.infrastructure.ShareLinkRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ShareLinkService {
	/**
	 * TODO: 리팩토링 필요
	 * 1. switch + default 예외가 여러 군데 반복됨
	 * 2. HttpServletResponse가 서비스 계층에 있음
	 * 3. Service가 “어떻게 쿠키를 만든다”까지 알고 있음
	 */
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
			.orElseThrow(() -> new BusinessException(CollectionError.INVALID_INVITATION_LINK));
	}

	@Transactional
	public void acceptInvitation(String code, Long userId) {
		ShareLink shareLink = findValidLink(code);
		acceptInvitationForTarget(shareLink, userId);
	}

	public ShareLink findValidLink(String code) {
		ShareLink shareLink = findShareLinkByCode(code);

		if (shareLink.isExpired()) {
			throw new BusinessException(CollectionError.EXPIRED_INVITATION_LINK);
		}
		return shareLink;
	}

	private void acceptInvitationForTarget(ShareLink shareLink, Long userId) {
		switch (shareLink.getTargetType()) {
			case COLLECTION:
				collectionService.addMemberFromShareLink(shareLink, userId);
				break;
			default:
				throw new BusinessException(CollectionError.INVALID_INVITATION_LINK);
		}
	}

	private String buildRedirectUrl(ShareLink shareLink) {
		String path = switch (shareLink.getTargetType()) {
			case COLLECTION -> "/collections/";
			default -> throw new BusinessException(CollectionError.INVALID_INVITATION_LINK);
		};
		return frontendBaseUrl + path + shareLink.getTargetId();
	}
}
