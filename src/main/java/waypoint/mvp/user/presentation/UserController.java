package waypoint.mvp.user.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.user.application.UserService;
import waypoint.mvp.user.application.dto.request.UserUpdateRequest;
import waypoint.mvp.user.application.dto.response.PresignedUrlResponse;
import waypoint.mvp.user.application.dto.response.UserResponse;

@Validated
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

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PutMapping("/me")
	public ResponseEntity<UserResponse> updateNickname(
		@AuthenticationPrincipal UserPrincipal user,
		@RequestBody @Valid UserUpdateRequest request
	) {
		return ResponseEntity.ok(userService.updateNickname(user, request.nickname()));
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PatchMapping("/me/picture")
	public ResponseEntity<PresignedUrlResponse> presignProfileImage(
		@AuthenticationPrincipal UserPrincipal user,
		@RequestParam @NotBlank String contentType
	) {
		return ResponseEntity.ok(userService.updateProfilePicture(user, contentType));
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@DeleteMapping("/me/picture")
	public ResponseEntity<Void> deleteProfileImage(
		@AuthenticationPrincipal UserPrincipal user
	) {
		userService.deleteProfilePicture(user);
		return ResponseEntity.noContent().build();
	}
}
