package waypoint.mvp.sharelink.infrastructure;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.sharelink.domain.ShareLink;
import waypoint.mvp.sharelink.domain.ShareLink.ShareLinkType;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
	Optional<ShareLink> findByCode(String code);

	@Query("""
			select count(sl) > 0
			from ShareLink sl
			where sl.code = :code
			  and sl.targetType = :targetType
			  and sl.targetId = :targetId
			  and sl.expiresAt > :now
		""")
	boolean existsValidShareLink(
		@Param("code") String code,
		@Param("targetType") ShareLinkType targetType,
		@Param("targetId") Long targetId,
		@Param("now") Instant now
	);

}
