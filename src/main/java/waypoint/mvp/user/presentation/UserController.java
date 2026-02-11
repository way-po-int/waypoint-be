package waypoint.mvp.user.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.user.application.UserService;
import waypoint.mvp.user.application.dto.UserResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@GetMapping("/me")
	public ResponseEntity<UserResponse> findMe(
		@AuthenticationPrincipal UserPrincipal user
	) {
		return ResponseEntity.ok(userService.findMe(user));
	}
}
