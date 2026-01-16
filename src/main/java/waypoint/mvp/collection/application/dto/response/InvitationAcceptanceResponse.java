package waypoint.mvp.collection.application.dto.response;

public record InvitationAcceptanceResponse(String redirectUrl) {

    public static InvitationAcceptanceResponse from(Long collectionId) {
        return new InvitationAcceptanceResponse("/collections/" + collectionId);
    }
}
