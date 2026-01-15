package waypoint.mvp.user.application;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import waypoint.mvp.global.annotation.ServiceTest;
import waypoint.mvp.user.application.dto.SocialUserProfile;
import waypoint.mvp.user.domain.Provider;
import waypoint.mvp.user.domain.SocialAccount;
import waypoint.mvp.user.domain.User;
import waypoint.mvp.user.infrastructure.persistence.UserRepository;

@ServiceTest
class UserServiceTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

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
}
