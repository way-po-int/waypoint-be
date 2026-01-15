package waypoint.mvp.auth.security.principal;

import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import waypoint.mvp.user.domain.Provider;
import waypoint.mvp.user.domain.User;

@EqualsAndHashCode(callSuper = true)
@Getter
public class CustomOidcUser extends DefaultOidcUser {

	private final Long id;
	private final Provider provider;
	private final String nickname;

	public CustomOidcUser(OidcUser oidcUser, User user) {
		super(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo());
		this.id = user.getId();
		this.provider = user.getSocialAccount().getProvider();
		this.nickname = user.getNickname();
	}
}
