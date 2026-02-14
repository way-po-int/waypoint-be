package waypoint.mvp.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.user.application.dto.SocialUserProfile;
import waypoint.mvp.user.application.dto.response.PresignedUrlResponse;
import waypoint.mvp.user.application.dto.response.UserResponse;
import waypoint.mvp.user.domain.SocialAccount;
import waypoint.mvp.user.domain.User;
import waypoint.mvp.user.error.UserError;
import waypoint.mvp.user.infrastructure.persistence.UserRepository;

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

		if (!result.pictureUrl().equals(me.getPicture())) {
			me.changePicture(result.pictureUrl());
		}

		return PresignedUrlResponse.from(result.presignedUrl());
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
