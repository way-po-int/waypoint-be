package waypoint.mvp.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.jwt.JwtAuthenticationFilter;
import waypoint.mvp.auth.security.jwt.JwtTokenProvider;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CorsConfigurationSource corsConfigurationSource;

	private final OidcUserService oidcUserService;
	private final SimpleUrlAuthenticationSuccessHandler successHandler;
	private final SimpleUrlAuthenticationFailureHandler failureHandler;

	private final AuthenticationEntryPoint authenticationEntryPoint;
	private final AccessDeniedHandler accessDeniedHandler;
	private final JwtTokenProvider jwtTokenProvider;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
					.oidcUserService(oidcUserService))
				.successHandler(successHandler)
				.failureHandler(failureHandler))
			.exceptionHandling(handler -> handler
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/dev/**").permitAll()
				.requestMatchers("/auth/reissue").permitAll()
				.anyRequest().authenticated())
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
			.build();
	}
}
