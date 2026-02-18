package waypoint.mvp.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.user.application.dto.SocialUserProfile;
import waypoint.mvp.user.application.dto.response.PresignedUrlResponse;
import waypoint.mvp.user.application.dto.response.UserResponse;
import waypoint.mvp.user.domain.SocialAccount;
import waypoint.mvp.user.domain.User;
import waypoint.mvp.user.error.UserError;
import waypoint.mvp.user.infrastructure.persistence.UserRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService implements UserFinder {

	private final UserRepository userRepository;
	private final UserProfileImageService userProfileImageService;

	@Transactional
	public User loadSocialUser(SocialUserProfile profile) {
		SocialAccount account = profile.socialAccount();
		return userRepository.findByProviderAndProviderId(account.getProvider(), account.getProviderId())
			.orElseGet(() -> userRepository.save(User.create(
				account,
				profile.nickname(),
				profile.picture(),
				profile.email()
			)));
	}

	@Transactional
	public UserResponse updateNickname(UserPrincipal user, String nickname) {
		User me = findById(user.id());
		me.changeNickname(nickname);
		return UserResponse.from(me);
	}

	@Transactional
	public PresignedUrlResponse updateProfilePicture(UserPrincipal user, String contentType) {
		User me = findById(user.id());

		var result = userProfileImageService.presignProfileUpload(me.getExternalId(), contentType);

		me.changePicture(result.pictureUrl());

		return PresignedUrlResponse.from(result.presignedUrl());
	}

	@Transactional
	public void deleteProfilePicture(UserPrincipal user) {
		User me = findById(user.id());

		String old = me.getPicture();
		me.changePicture("");

		if (old == null || old.isBlank()) {
			return;
		}

		Long userId = user.id();
		String externalId = me.getExternalId();

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				try {
					userProfileImageService.deleteProfileImageIfManaged(externalId, old);
				} catch (Exception e) {
					log.warn("Failed to delete profile image from S3 after commit. userId={}, url={}", userId, old, e);
				}
			}
		});
	}

	public UserResponse findMe(UserPrincipal user) {
		User me = findById(user.id());
		return UserResponse.from(me);
	}

	@Override
	public User findById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));
	}
}
