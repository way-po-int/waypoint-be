package waypoint.mvp.place.application.dto.content;

import waypoint.mvp.place.domain.content.ContentSnapshot;

public interface RawContent {

	ContentSnapshot toSnapshot();
}
