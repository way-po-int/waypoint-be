package waypoint.mvp.sharelink.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.sharelink.domain.ShareLink;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
}
