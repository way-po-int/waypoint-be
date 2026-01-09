package waypoint.mvp.user.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import waypoint.mvp.user.domain.Provider;
import waypoint.mvp.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u WHERE u.socialAccount.provider = :provider AND u.socialAccount.providerId = :providerId")
	Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
