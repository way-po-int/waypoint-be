package waypoint.mvp.auth.security.service.strategy;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2Strategy {
	boolean support(String registrationId);

	OAuth2User loadUser(OAuth2UserRequest userRequest);

	default void normalizeClaim(Map<String, Object> attributes, String standardKey, String legacyKey) {
		attributes.computeIfAbsent(standardKey, key -> attributes.get(legacyKey));
		attributes.remove(legacyKey);
	}
}
