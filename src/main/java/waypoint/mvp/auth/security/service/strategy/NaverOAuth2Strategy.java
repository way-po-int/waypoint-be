package waypoint.mvp.auth.security.service.strategy;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverOAuth2Strategy implements OAuth2Strategy {

	private final RestClient restClient;

	@Override
	public boolean support(String registrationId) {
		return "naver".equals(registrationId);
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) {
		String userInfoUri = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUri();
		String tokenValue = userRequest.getAccessToken()
			.getTokenValue();
		ResponseEntity<NaverUserInfoDto> response = restClient.get()
			.uri(userInfoUri)
			.headers(httpHeaders -> httpHeaders.setBearerAuth(tokenValue))
			.retrieve()
			.toEntity(NaverUserInfoDto.class);
		if (response.hasBody()) {
			NaverUserInfoDto oauthResponse = response.getBody();
			if (oauthResponse != null && oauthResponse.response() instanceof Map<String, Object> userAttributes) {
				normalizeClaim(userAttributes, StandardClaimNames.SUB, "id");
				normalizeClaim(userAttributes, StandardClaimNames.PICTURE, "profile_image");
				Set<GrantedAuthority> authorities = new LinkedHashSet<>();
				authorities.add(new OAuth2UserAuthority(userAttributes));
				OAuth2AccessToken token = userRequest.getAccessToken();
				for (String authority : token.getScopes()) {
					authorities.add(new SimpleGrantedAuthority("SCOPE_" + authority));
				}
				return new DefaultOAuth2User(authorities, userAttributes, StandardClaimNames.SUB);
			}
		}
		log.error("네이버 응답 형식 불일치. headers={}, body={}", response.getHeaders(), response.getBody());
		throw new OAuth2AuthenticationException("Naver UserInfo response is invalid");
		// https://developers.naver.com/docs/login/web/web.md
	}

	private record NaverUserInfoDto(
		String resultcode,
		String message,
		Map<String, Object> response) {
	}
}
