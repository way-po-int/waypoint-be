package waypoint.mvp.user.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.annotation.ServiceTest;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.global.util.MaskingUtils;
import waypoint.mvp.user.application.dto.SocialUserProfile;
import waypoint.mvp.user.application.dto.response.PresignedUrlResponse;
import waypoint.mvp.user.application.dto.response.UserResponse;
import waypoint.mvp.user.domain.Provider;
import waypoint.mvp.user.domain.SocialAccount;
import waypoint.mvp.user.domain.User;
import waypoint.mvp.user.error.UserError;
import waypoint.mvp.user.infrastructure.persistence.UserRepository;

@ServiceTest
class UserServiceTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@MockitoBean
	private UserProfileImageService userProfileImageService;

	@Test
	@DisplayName("소셜 로그인 시 신규 유저라면 DB에 새로 저장된다.")
	void loadSocialUser_success() {
		// given
		SocialAccount socialAccount = SocialAccount.create(Provider.GOOGLE, "1");
		String nickname = "구글";
		String picture = "https://profile.image/new.jpg";
		String email = "test@test.com";
		SocialUserProfile profile = new SocialUserProfile(socialAccount, nickname, picture, email);

		// when
		userService.loadSocialUser(profile);

		// then
		Optional<User> savedUser = userRepository.findByProviderAndProviderId(
			socialAccount.getProvider(), socialAccount.getProviderId());
		assertThat(savedUser)
			.isPresent()
			.hasValueSatisfying(user -> {
				assertThat(user.getNickname()).isEqualTo(nickname);
				assertThat(user.getPicture()).isEqualTo(picture);
				assertThat(user.getEmail()).isEqualTo(email);
			});
	}

	@Test
	@DisplayName("소셜 로그인 시 기존 유저라면 소셜 프로필 정보로 갱신되지 않는다.")
	void loadSocialUser_doesNotOverwrite_success() {
		// given
		SocialAccount socialAccount = SocialAccount.create(Provider.GOOGLE, "1");
		SocialUserProfile newProfile = SocialUserProfile.of(socialAccount, "새로운 이름", "", "");
		String originalName = "기존 유저";
		userRepository.save(User.create(socialAccount, originalName, "", ""));

		// when
		userService.loadSocialUser(newProfile);

		// then
		Optional<User> savedUser = userRepository.findByProviderAndProviderId(
			socialAccount.getProvider(), socialAccount.getProviderId());
		assertThat(savedUser).isPresent();
		assertThat(savedUser.get().getNickname()).isEqualTo(originalName);
	}

	@Test
	@DisplayName("내 정보 조회 시 유저 상세 정보를 반환한다.")
	void findMe_success() {
		// given
		String providerId = UUID.randomUUID().toString();
		SocialAccount socialAccount = SocialAccount.create(Provider.GOOGLE, providerId);
		String email = "test@test.com";

		User saved = userRepository.save(User.create(socialAccount, "닉", "pic", email));
		UserPrincipal principal = new UserPrincipal(saved.getId());

		// when
		UserResponse response = userService.findMe(principal);

		// then
		assertThat(response.userId()).isEqualTo(saved.getExternalId());
		assertThat(response.provider()).isEqualTo(Provider.GOOGLE);
		assertThat(response.nickname()).isEqualTo("닉");
		assertThat(response.picture()).isEqualTo("pic");
		assertThat(response.email()).isEqualTo(MaskingUtils.maskEmail(email));
	}

	@Test
	@DisplayName("내 정보 조회 시 존재하지 않는 유저면 USER_NOT_FOUND 예외가 발생한다.")
	void findMe_fail_userNotFound() {
		// given
		UserPrincipal principal = new UserPrincipal(999999L);

		// when & then
		assertThatThrownBy(() -> userService.findMe(principal))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getBody().getProperties().get("code"))
			.isEqualTo(UserError.USER_NOT_FOUND.name());
	}

	@Test
	@DisplayName("닉네임 변경 시 DB에 반영된다.")
	void updateNickname_success() {
		// given
		String providerId = UUID.randomUUID().toString();
		User saved = userRepository.save(User.create(
			SocialAccount.create(Provider.GOOGLE, providerId),
			"기존닉네임",
			"",
			"test@test.com"
		));
		UserPrincipal principal = new UserPrincipal(saved.getId());

		// when
		UserResponse response = userService.updateNickname(principal, "새닉네임");

		// then
		assertThat(response.nickname()).isEqualTo("새닉네임");

		User reloaded = userRepository.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.getNickname()).isEqualTo("새닉네임");
	}

	@Test
	@DisplayName("프로필 이미지 변경 시 presignedUrl을 반환하고 pictureUrl을 DB에 저장한다.")
	void updateProfilePicture_success() {
		// given
		String providerId = UUID.randomUUID().toString();
		User saved = userRepository.save(User.create(
			SocialAccount.create(Provider.GOOGLE, providerId),
			"닉네임",
			"",
			"test@test.com"
		));
		UserPrincipal principal = new UserPrincipal(saved.getId());

		String contentType = "image/png";
		String presignedUrl = "https://presigned.test";
		String pictureUrl = "https://cdn.test/users/%s/profile/%s.png"
			.formatted(saved.getExternalId(), UUID.randomUUID());

		given(userProfileImageService.presignProfileUpload(saved.getExternalId(), contentType))
			.willReturn(new UserProfileImageService.PresignedUpload(presignedUrl, pictureUrl));

		// when
		PresignedUrlResponse response = userService.updateProfilePicture(principal, contentType);

		// then
		assertThat(response.presignedUrl()).isEqualTo(presignedUrl);

		User reloaded = userRepository.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.getPicture()).isEqualTo(pictureUrl);

		then(userProfileImageService).should()
			.presignProfileUpload(saved.getExternalId(), contentType);
	}

	@Test
	@DisplayName("이미지 삭제 시 DB의 picture를 비우고, 커밋 후 S3 삭제를 시도한다.")
	void deleteProfilePicture_success_callsS3Delete() {
		// given
		String providerId = UUID.randomUUID().toString();
		User saved = userRepository.save(User.create(
			SocialAccount.create(Provider.GOOGLE, providerId),
			"닉네임",
			"",
			"test@test.com"
		));
		String pictureUrl = "https://way-point-bucket.s3.ap-northeast-2.amazonaws.com/users/%s/profile/%s.png"
			.formatted(saved.getExternalId(), UUID.randomUUID());

		saved.changePicture(pictureUrl);
		userRepository.saveAndFlush(saved);

		UserPrincipal principal = new UserPrincipal(saved.getId());

		String oldPicture = saved.getPicture();
		String externalId = saved.getExternalId();

		// when
		userService.deleteProfilePicture(principal);

		// then
		User reloaded = userRepository.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.getPicture()).isEmpty();

		then(userProfileImageService).should()
			.deleteProfileImageIfManaged(externalId, oldPicture);
	}

	@Test
	@DisplayName("이미지 삭제 시 S3 오류가 발생해도 유저 정보는 정상적으로 비워진다.")
	void deleteProfilePicture_ignoreS3Failure_success() {
		// given
		String providerId = UUID.randomUUID().toString();
		User saved = userRepository.save(User.create(
			SocialAccount.create(Provider.GOOGLE, providerId),
			"닉네임",
			"",
			"test@test.com"
		));
		String pictureUrl = "https://way-point-bucket.s3.ap-northeast-2.amazonaws.com/users/%s/profile/%s.png"
			.formatted(saved.getExternalId(), UUID.randomUUID());

		saved.changePicture(pictureUrl);
		userRepository.saveAndFlush(saved);

		UserPrincipal principal = new UserPrincipal(saved.getId());

		String oldPicture = saved.getPicture();
		String externalId = saved.getExternalId();

		willThrow(new RuntimeException("s3 down"))
			.given(userProfileImageService).deleteProfileImageIfManaged(externalId, oldPicture);

		// when & then
		assertThatCode(() -> userService.deleteProfilePicture(principal))
			.doesNotThrowAnyException();

		User reloaded = userRepository.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.getPicture()).isEmpty();

		then(userProfileImageService).should()
			.deleteProfileImageIfManaged(externalId, oldPicture);
	}
}
