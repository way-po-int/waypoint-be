package waypoint.mvp.collection.application;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.application.dto.request.CollectionCreateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionRole;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.annotation.ServiceTest;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.sharelink.application.dto.response.ShareLinkResponse;
import waypoint.mvp.sharelink.domain.ShareLink;
import waypoint.mvp.sharelink.infrastructure.ShareLinkRepository;
import waypoint.mvp.user.domain.Provider;
import waypoint.mvp.user.domain.SocialAccount;
import waypoint.mvp.user.domain.User;
import waypoint.mvp.user.infrastructure.persistence.UserRepository;

@ServiceTest
class CollectionServiceTest {

	@Autowired
	private CollectionService collectionService;

	@Autowired
	private CollectionRepository collectionRepository;

	@Autowired
	private CollectionMemberRepository collectionMemberRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ShareLinkRepository shareLinkRepository;

	private User baseUser;

	@BeforeEach
	void setUp() {
		SocialAccount socialAccount = SocialAccount.create(Provider.GOOGLE, "12345");
		User user = User.create(socialAccount, "tester", "picture_url", "test@example.com");
		baseUser = userRepository.save(user);
	}

	@Test
	@DisplayName("컬렉션을 생성하면, 생성자가 소유주인 멤버로 함께 등록된다.")
	void createCollection_withMember_success() {
		// given
		String title = "My First Collection";
		UserPrincipal requestPrincipal = new UserPrincipal(baseUser.getId());

		// when
		Collection collection = createCollection(title, requestPrincipal);

		// then
		// 1. Collection이 정상적으로 저장되었는지 검증
		Optional<Collection> foundCollection = collectionRepository.findById(collection.getId());
		assertThat(foundCollection).isPresent();
		assertThat(foundCollection.get().getTitle()).isEqualTo(title);

		// 2. CollectionMember가 정상적으로 저장되었는지 검증 (이벤트 리스너 동작 확인)
		Optional<CollectionMember> foundMember = collectionMemberRepository.findActiveByUserId(collection.getId(),
			baseUser.getId());
		assertThat(foundMember).isPresent();
		assertThat(foundMember.get().getRole()).isEqualTo(CollectionRole.OWNER);
		assertThat(foundMember.get().getUser().getId()).isEqualTo(baseUser.getId());
	}

	@Test
	@DisplayName("컬렉션 멤버는 성공적으로 초대 링크를 생성할 수 있다.")
	void createInvitation_success() {
		// given
		User anotherUser = createUser("anotherUser");
		UserPrincipal anotherPrincipal = new UserPrincipal(anotherUser.getId());
		UserPrincipal ownerPrincipal = new UserPrincipal(baseUser.getId());

		// 2. 컬렉션 생성 및 소유자, 일반 멤버 등록
		Collection collection = createCollection("Test Collection", ownerPrincipal);
		collectionMemberRepository.save(CollectionMember.create(collection, anotherUser, CollectionRole.MEMBER));

		// when & then
		// Case 1: 소유자가 생성
		ShareLinkResponse ownerResponse = collectionService.createInvitation(collection.getId(), ownerPrincipal);
		Optional<ShareLink> ownerLink = shareLinkRepository.findByCode(ownerResponse.code());
		assertThat(ownerLink).isPresent();
		assertThat(ownerLink.get().getHostUserId()).isEqualTo(ownerPrincipal.getId());

		// Case 2: 일반 멤버가 생성
		ShareLinkResponse memberResponse = collectionService.createInvitation(collection.getId(), anotherPrincipal);
		Optional<ShareLink> memberLink = shareLinkRepository.findByCode(memberResponse.code());
		assertThat(memberLink).isPresent();
		assertThat(memberLink.get().getHostUserId()).isEqualTo(anotherUser.getId());
	}

	@Test
	@DisplayName("컬렉션 멤버가 아니면 초대 링크를 생성할 수 없다.")
	void createInvitation_fail_notMember() {
		// given
		// 1. 다른 유저 생성
		UserPrincipal anotherPrinical = new UserPrincipal(createUser("anotherUser").getId());
		UserPrincipal ownerPrincipal = new UserPrincipal(baseUser.getId());

		// 2. 컬렉션 생성 (anotherUser는 멤버가 아님)
		Collection collection = createCollection("Test Collection", ownerPrincipal);

		// when & then
		assertThatThrownBy(
			() -> collectionService.createInvitation(collection.getId(), anotherPrinical))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getBody().getProperties().get("code"))
			.isEqualTo(CollectionError.FORBIDDEN_NOT_MEMBER.name());
	}

	private User createUser(String userName) {
		SocialAccount socialAccount = SocialAccount.create(Provider.GOOGLE, userName + "_12345");
		User user = User.create(socialAccount, userName, "picture_url", userName + "@example.com");
		return userRepository.save(user);
	}

	private Collection createCollection(String title, UserPrincipal ownerUser) {
		CollectionCreateRequest createRequest = new CollectionCreateRequest(title);
		CollectionResponse CollectionResponse = collectionService.createCollection(createRequest, ownerUser);

		return findCollectionById(CollectionResponse.id());

	}

	private Collection findCollectionById(Long collectionId) {
		return collectionRepository.findById(collectionId).get();
	}
}
