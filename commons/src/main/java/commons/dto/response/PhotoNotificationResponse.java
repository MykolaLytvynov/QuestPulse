package commons.dto.response;

public record PhotoNotificationResponse(
        String chatId,
        byte[] photoBytes,
        String fileName
) {
}
