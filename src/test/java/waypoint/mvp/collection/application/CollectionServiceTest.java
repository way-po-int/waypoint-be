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
		CollectionCreateRequest createRequest = new CollectionCreateRequest(title);

		// when
		CollectionResponse response = collectionService.createCollection(createRequest, requestPrincipal);

		// then
		// 1. 응답 값 검증 (가장 중요)
		assertThat(response.title()).isEqualTo(title);
		assertThat(response.memberCount()).isEqualTo(1);

		// 2. DB 데이터 검증 (Side Effect 검증)
		Optional<Collection> foundCollection = collectionRepository.findById(response.id());
		assertThat(foundCollection).isPresent();
		assertThat(foundCollection.get().getTitle()).isEqualTo(title);
		assertThat(foundCollection.get().getMemberCount()).isEqualTo(1);

		CollectionMember foundMember = findActiveMember(response.id(), baseUser.getId());
		assertThat(foundMember.getRole()).isEqualTo(CollectionRole.OWNER);
		assertThat(foundMember.getUser().getId()).isEqualTo(baseUser.getId());
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
		ShareLinkResponse ownerResponse = collectionService.createInvitation(collection.getExternalId(),
			ownerPrincipal);
		Optional<ShareLink> ownerLink = shareLinkRepository.findByCode(ownerResponse.code());
		assertThat(ownerLink).isPresent();
		assertThat(ownerLink.get().getHostUserId()).isEqualTo(ownerPrincipal.getId());

		// Case 2: 일반 멤버가 생성
		ShareLinkResponse memberResponse = collectionService.createInvitation(collection.getExternalId(),
			anotherPrincipal);
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
			() -> collectionService.createInvitation(collection.getExternalId(), anotherPrinical))
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
		CollectionResponse collectionResponse = collectionService.createCollection(createRequest, ownerUser);

		return findCollectionById(collectionResponse.id());

	}

	private Collection findCollectionById(Long collectionId) {
		return collectionRepository.findById(collectionId).orElseThrow();
	}

	private CollectionMember findActiveMember(long collectionId, long userId) {
		return collectionMemberRepository.findActiveByUserId(collectionId, userId).orElseThrow();
	}

	@Test
	@DisplayName("멤버가 자발적으로 컬렉션을 탈퇴하면 soft delete되고 멤버 수가 감소한다.")
	void withdrawCollectionMember_byMember_success() {
		// given
		UserPrincipal owner = new UserPrincipal(baseUser.getId());
		UserPrincipal member = new UserPrincipal(createUser("member1").getId());
		Collection collection = createCollectionAndInvitedMember("Test Collection", owner, member);

		// when
		collectionService.withdrawCollectionMember(collection.getId(), member);

		// then
		Optional<CollectionMember> withdrawnMember = collectionMemberRepository.findWithdrawnMember(collection.getId(),
			member.getId());
		assertThat(withdrawnMember).isPresent();
		assertThat(withdrawnMember.get().getDeletedAt()).isNotNull();

		Collection updatedCollection = collectionRepository.findById(collection.getId()).orElseThrow();
		assertThat(updatedCollection.getMemberCount()).isEqualTo(1);

		// 활성 멤버 조회 시, 탈퇴한 멤버가 조회되지 않는지 추가 검증
		assertThat(collectionMemberRepository.findActiveByUserId(collection.getId(), member.getId())).isNotPresent();
	}

	@Test
	@DisplayName("Owner가 컬렉션을 탈퇴하려고 하면 소유권 위임 필요 예외가 발생한다.")
	void withdrawCollectionMember_byOwner_fail_needToDelegate() {
		// given
		UserPrincipal owner = new UserPrincipal(baseUser.getId());
		UserPrincipal member = new UserPrincipal(createUser("member1").getId());
		Collection collection = createCollectionAndInvitedMember("Test Collection", owner, member);

		// when & then
		assertThatThrownBy(() -> collectionService.withdrawCollectionMember(collection.getId(), owner))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getBody().getProperties().get("code"))
			.isEqualTo(CollectionError.NEED_TO_DELEGATE_OWNERSHIP.name());
	}

	@Test
	@DisplayName("Owner가 다른 멤버를 강제로 탈퇴시킬 수 있다.")
	void expelCollectionMember_byOwner_success() {
		// given
		UserPrincipal owner = new UserPrincipal(baseUser.getId());
		UserPrincipal member = new UserPrincipal(createUser("member1").getId());
		Collection collection = createCollectionAndInvitedMember("Test Collection", owner, member);

		// when
		collectionService.expelCollectionMember(collection.getId(), member.getId(), owner);

		// then
		Optional<CollectionMember> expelledMember = collectionMemberRepository.findWithdrawnMember(collection.getId(),
			member.getId());
		assertThat(expelledMember).isPresent();
		assertThat(expelledMember.get().getDeletedAt()).isNotNull();

		Collection updatedCollection = collectionRepository.findById(collection.getId()).orElseThrow();
		assertThat(updatedCollection.getMemberCount()).isEqualTo(1);

		// 활성 멤버 조회 시, 추방된 멤버가 조회되지 않는지 추가 검증
		assertThat(collectionMemberRepository.findActiveByUserId(collection.getId(), member.getId())).isNotPresent();
	}

	@Test
	@DisplayName("탈퇴했던 멤버가 다시 초대되면, 기존 데이터가 복구되고 멤버 수가 증가한다.")
	void addCollectionMember_forWithdrawnMember_restores() {
		// given
		UserPrincipal host = new UserPrincipal(baseUser.getId());
		UserPrincipal member = new UserPrincipal(createUser("member1").getId());

		Collection collection = createCollectionAndInvitedMember("Test Collection", host, member);
		collectionService.withdrawCollectionMember(collection.getId(), member); // 멤버 탈퇴

		// when 재초대
		ShareLink shareLink = shareLinkRepository.findByCode(
			collectionService.createInvitation(collection.getExternalId(), host).code()).get();
		collectionService.addMemberFromShareLink(shareLink, member.getId());

		// then
		CollectionMember restoredMember = findActiveMember(collection.getId(), member.getId());
		assertThat(restoredMember.getDeletedAt()).isNull();

		Collection updatedCollection = collectionRepository.findById(collection.getId()).orElseThrow();
		assertThat(updatedCollection.getMemberCount()).isEqualTo(2);
	}

	private Collection createCollectionAndInvitedMember(String title, UserPrincipal owner, UserPrincipal member) {
		Collection collection = createCollection(title, owner);

		ShareLink shareLink = shareLinkRepository.findByCode(
			collectionService.createInvitation(collection.getExternalId(), owner).code()).orElseThrow();
		collectionService.addMemberFromShareLink(shareLink, member.getId());

		return findCollectionById(collection.getId()); // 멤버 추가 후 최신 상태의 컬렉션을 반환
	}

	@Test
	@DisplayName("Owner는 다른 멤버에게 소유권을 위임할 수 있다.")
	void changeOwner_byOwner_success() {
		// given
		UserPrincipal owner = new UserPrincipal(baseUser.getId());
		User newOwnerUser = createUser("newOwner");
		UserPrincipal newOwner = new UserPrincipal(newOwnerUser.getId());
		Collection collection = createCollectionAndInvitedMember("Test Collection", owner, newOwner);
		CollectionMember newOwnerMember = findActiveMember(collection.getId(), newOwner.getId());

		// when
		collectionService.changeOwner(collection.getExternalId(), newOwnerMember.getExternalId(), owner);

		// then
		CollectionMember formerOwnerMember = findActiveMember(collection.getId(), owner.getId());
		CollectionMember currentOwnerMember = findActiveMember(collection.getId(), newOwner.getId());

		assertThat(formerOwnerMember.getRole()).isEqualTo(CollectionRole.MEMBER);
		assertThat(currentOwnerMember.getRole()).isEqualTo(CollectionRole.OWNER);
	}

	@Test
	@DisplayName("Owner가 아니면 소유권을 위임할 수 없다.")
	void changeOwner_byMember_fail() {
		// given
		UserPrincipal owner = new UserPrincipal(baseUser.getId());
		UserPrincipal member = new UserPrincipal(createUser("member1").getId());
		Collection collection = createCollectionAndInvitedMember("Test Collection", owner, member);
		CollectionMember ownerMember = findActiveMember(collection.getId(), owner.getId());

		// when & then
		assertThatThrownBy(
			() -> collectionService.changeOwner(collection.getExternalId(), ownerMember.getExternalId(), member))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getBody().getProperties().get("code"))
			.isEqualTo(CollectionError.FORBIDDEN_NOT_OWNER.name());
	}

}
