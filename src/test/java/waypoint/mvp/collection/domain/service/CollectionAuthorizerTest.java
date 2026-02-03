package waypoint.mvp.collection.domain.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.GuestPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionRole;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.config.AuthorizerConfig.AuthorizerErrorCodes;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;
import waypoint.mvp.user.domain.User;

@ExtendWith(MockitoExtension.class)
class CollectionAuthorizerTest {

	@InjectMocks
	private ResourceAuthorizer collectionAuthorizer;

	@Mock
	private CollectionMemberRepository memberRepository;

	private final Long collectionId = 1L;
	private final Long userId = 100L;

	@Mock
	private User user;

	@Mock
	private Collection collection;
	private AuthPrincipal loggedInUser;
	private AuthPrincipal guestUser;

	@BeforeEach
	void setUp() {
		// MockitoAnnotations.openMocks(this)가 호출된 후 직접 생성
		collectionAuthorizer = new ResourceAuthorizer(
			memberRepository::findActiveByUserId,
			memberRepository::existsActive,
			ShareLinkType.COLLECTION,
			new AuthorizerErrorCodes(
				CollectionError.FORBIDDEN_NOT_OWNER,
				CollectionError.FORBIDDEN_NOT_MEMBER,
				CollectionError.MEMBER_ALREADY_EXISTS,
				CollectionError.FORBIDDEN_NOT_GUEST
			)
		);

		loggedInUser = new UserPrincipal(userId);
		guestUser = new GuestPrincipal("test-guest-code", ShareLinkType.COLLECTION, collectionId);
	}

	@Nested
	@DisplayName("verifyAccess (Guest 또는 Member 접근 검증)")
	class VerifyAccess {

		@Test
		@DisplayName("성공: 유효한 Guest는 접근할 수 있다.")
		void success_whenValidGuest() {
			// given
			// when & then
			assertThatCode(() -> collectionAuthorizer.verifyAccess(guestUser, collectionId))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("실패: 다른 리소스를 가리키는 Guest는 접근할 수 없다.")
		void fail_whenGuestWithDifferentTarget() {
			// given
			AuthPrincipal invalidGuest = new GuestPrincipal("invalid-code", ShareLinkType.COLLECTION, 999L);

			// when & then
			assertThatThrownBy(() -> collectionAuthorizer.verifyAccess(invalidGuest, collectionId))
				.isInstanceOf(BusinessException.class);
		}

		@Test
		@DisplayName("성공: 컬렉션 멤버는 접근할 수 있다.")
		void success_whenCollectionMember() {
			// given
			given(memberRepository.existsActive(collectionId, userId)).willReturn(true);

			// when & then
			assertThatCode(() -> collectionAuthorizer.verifyAccess(loggedInUser, collectionId))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("실패: 멤버가 아닌 사용자는 접근할 수 없다.")
		void fail_whenNotCollectionMember() {
			// given
			given(memberRepository.existsActive(collectionId, userId)).willReturn(false);

			// when & then
			assertThatThrownBy(() -> collectionAuthorizer.verifyAccess(loggedInUser, collectionId))
				.isInstanceOf(BusinessException.class);
		}
	}

	@Nested
	@DisplayName("verifyOwner (소유자 접근 검증)")
	class VerifyOwner {

		@Test
		@DisplayName("성공: 소유자는 접근할 수 있다.")
		void success_whenOwner() {
			// given
			CollectionMember ownerMember = CollectionMember.create(collection, user, CollectionRole.OWNER);
			given(memberRepository.findActiveByUserId(collectionId, userId)).willReturn(
				Optional.of(ownerMember));

			// when & then
			assertThatCode(() -> collectionAuthorizer.verifyOwner(loggedInUser, collectionId))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("실패: 멤버는 소유자가 아니므로 접근할 수 없다.")
		void fail_whenMember() {
			// given
			CollectionMember member = CollectionMember.create(collection, user, CollectionRole.MEMBER);
			given(memberRepository.findActiveByUserId(collectionId, userId)).willReturn(Optional.of(member));

			// when & then
			assertThatThrownBy(() -> collectionAuthorizer.verifyOwner(loggedInUser, collectionId))
				.isInstanceOf(BusinessException.class);
		}

		@Test
		@DisplayName("실패: Guest는 소유자가 아니므로 접근할 수 없다.")
		void fail_whenGuest() {
			// given
			// when & then
			assertThatThrownBy(() -> collectionAuthorizer.verifyOwner(guestUser, collectionId))
				.isInstanceOf(BusinessException.class);
		}
	}
}
