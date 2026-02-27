package waypoint.mvp.auth.security.exception;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import waypoint.mvp.auth.error.AuthErrorCode;

public class OAuth2UserWithdrawnException extends OAuth2AuthenticationException {

	public OAuth2UserWithdrawnException() {
		super(new OAuth2Error(
			"user_withdrawn",
			AuthErrorCode.USER_WITHDRAWN.getMessage(),
			null
		));
	}
}
