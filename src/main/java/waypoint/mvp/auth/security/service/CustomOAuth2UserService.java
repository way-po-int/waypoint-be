package waypoint.mvp.auth.security.service;

import java.util.List;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.service.strategy.OAuth2Strategy;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final List<OAuth2Strategy> strategies;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		return strategies.stream()
			.filter(s -> s.support(registrationId))
			.findFirst()
			.map(strategy -> strategy.loadUser(userRequest))
			.orElseGet(() -> super.loadUser(userRequest));
	}
}
