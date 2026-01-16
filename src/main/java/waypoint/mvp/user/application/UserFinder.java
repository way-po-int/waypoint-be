package waypoint.mvp.user.application;

import waypoint.mvp.user.domain.User;

public interface UserFinder {
    User findById(Long userId);
}
