package waypoint.mvp.collection.application;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import waypoint.mvp.auth.security.principal.UserInfo;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionRole;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.collection.presentation.dto.CollectionCreateRequest;
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
		Long collectionId = collectionService.createCollection(request, userInfo);

		// then
		// 1. Collection이 정상적으로 저장되었는지 검증
		Optional<Collection> foundCollection = collectionRepository.findById(collectionId);
		assertThat(foundCollection).isPresent();
		assertThat(foundCollection.get().getTitle()).isEqualTo(title);

		// 2. CollectionMember가 정상적으로 저장되었는지 검증 (이벤트 리스너 동작 확인)
		Optional<CollectionMember> foundMember = collectionMemberRepository.findByCollectionIdAndUserId(collectionId,
			savedUser.getId());
		assertThat(foundMember).isPresent();
		assertThat(foundMember.get().getRole()).isEqualTo(CollectionRole.OWNER);
		assertThat(foundMember.get().getUser().getId()).isEqualTo(savedUser.getId());
	}
}
