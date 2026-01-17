package waypoint.mvp.collection.application;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import waypoint.mvp.auth.security.principal.UserInfo;
import waypoint.mvp.collection.application.dto.request.CollectionCreateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionRole;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.sharelink.application.dto.response.ShareLinkResponse;
import waypoint.mvp.sharelink.domain.ShareLink;
import waypoint.mvp.sharelink.infrastructure.ShareLinkRepository;
import waypoint.mvp.global.annotation.ServiceTest;
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

	private User savedUser;

	@BeforeEach
	void setUp() {
		SocialAccount socialAccount = SocialAccount.create(Provider.GOOGLE, "12345");
		User user = User.create(socialAccount, "tester", "picture_url", "test@example.com");
		savedUser = userRepository.save(user);
	}

	@Test
	@DisplayName("컬렉션을 생성하면, 생성자가 소유주인 멤버로 함께 등록된다.")
	void createCollection_withMember_success() {
		// given
		String title = "My First Collection";
		CollectionCreateRequest request = new CollectionCreateRequest(title);
		UserInfo userInfo = new UserInfo(savedUser.getId());

		// when
		CollectionResponse collection = collectionService.createCollection(request, userInfo);

		// then
		// 1. Collection이 정상적으로 저장되었는지 검증
		Optional<Collection> foundCollection = collectionRepository.findById(collection.id());
		assertThat(foundCollection).isPresent();
		assertThat(foundCollection.get().getTitle()).isEqualTo(title);

		// 2. CollectionMember가 정상적으로 저장되었는지 검증 (이벤트 리스너 동작 확인)
		Optional<CollectionMember> foundMember = collectionMemberRepository.findByCollectionIdAndUserId(collection.id(),
			savedUser.getId());
		assertThat(foundMember).isPresent();
		assertThat(foundMember.get().getRole()).isEqualTo(CollectionRole.OWNER);
		assertThat(foundMember.get().getUser().getId()).isEqualTo(savedUser.getId());
	}

	@Test
	@DisplayName("컬렉션 멤버는 성공적으로 초대 링크를 생성할 수 있다.")
	void createInvitation_success() {
		// given
		// 1. 다른 유저(일반 멤버) 생성
		SocialAccount memberAccount = SocialAccount.create(Provider.GOOGLE, "member123");
		User memberUser = userRepository.save(User.create(memberAccount, "memberUser", "pic", "member@test.com"));

		// 2. 컬렉션 생성 및 소유자, 일반 멤버 등록
		Collection collection = collectionRepository.save(Collection.create("Test Collection"));
		collectionMemberRepository.save(CollectionMember.create(collection, savedUser, CollectionRole.OWNER));
		collectionMemberRepository.save(CollectionMember.create(collection, memberUser, CollectionRole.MEMBER));

		// when & then
		// Case 1: 소유자가 생성
		ShareLinkResponse ownerResponse = collectionService.createInvitation(collection.getId(), savedUser.getId());
		Optional<ShareLink> ownerLink = shareLinkRepository.findByCode(ownerResponse.code());
		assertThat(ownerLink).isPresent();
		assertThat(ownerLink.get().getHostUserId()).isEqualTo(savedUser.getId());

		// Case 2: 일반 멤버가 생성
		ShareLinkResponse memberResponse = collectionService.createInvitation(collection.getId(), memberUser.getId());
		Optional<ShareLink> memberLink = shareLinkRepository.findByCode(memberResponse.code());
		assertThat(memberLink).isPresent();
		assertThat(memberLink.get().getHostUserId()).isEqualTo(memberUser.getId());
	}

	@Test
	@DisplayName("컬렉션 멤버가 아니면 초대 링크를 생성할 수 없다.")
	void createInvitation_fail_notMember() {
		// given
		// 1. 다른 유저 생성
		SocialAccount anotherAccount = SocialAccount.create(Provider.GOOGLE, "54321");
		User anotherUser = userRepository.save(User.create(anotherAccount, "anotherUser", "pic", "another@test.com"));

		// 2. 컬렉션 생성 (anotherUser는 멤버가 아님)
		Collection collection = collectionRepository.save(Collection.create("Test Collection"));

		// when & then
		assertThatThrownBy(() -> collectionService.createInvitation(collection.getId(), anotherUser.getId()))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException) ex).getBody().getProperties().get("code"))
			.isEqualTo("FORBIDDEN_NOT_MEMBER");
	}
}
