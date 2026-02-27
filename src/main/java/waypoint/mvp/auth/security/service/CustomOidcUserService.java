package waypoint.mvp.auth.security.service;

import java.util.Objects;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import waypoint.mvp.auth.security.exception.OAuth2UserWithdrawnException;
import waypoint.mvp.auth.security.principal.CustomOidcUser;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.user.application.UserService;
import waypoint.mvp.user.application.dto.SocialUserProfile;
import waypoint.mvp.user.domain.Provider;
import waypoint.mvp.user.domain.SocialAccount;
import waypoint.mvp.user.domain.User;

@Service
public class CustomOidcUserService extends OidcUserService {

	private final UserService userService;

	public CustomOidcUserService(CustomOAuth2UserService oAuth2UserService, UserService userService) {
		setOauth2UserService(oAuth2UserService);
		this.userService = userService;
	}

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OidcUser oidcUser = super.loadUser(userRequest);
		Provider provider = Provider.from(userRequest.getClientRegistration().getRegistrationId());
		String providerId = oidcUser.getSubject();

		SocialAccount socialAccount = SocialAccount.create(provider, providerId);
		String nickname = Objects.requireNonNullElse(oidcUser.getNickName(), oidcUser.getFullName());

		SocialUserProfile profile = SocialUserProfile.of(
			socialAccount,
			nickname,
			oidcUser.getPicture(),
			oidcUser.getEmail()
		);
		try {
			User user = userService.loadSocialUser(profile);
			return new CustomOidcUser(oidcUser, user);
		} catch (BusinessException e) {
			throw new OAuth2UserWithdrawnException();
		}
	}
}
