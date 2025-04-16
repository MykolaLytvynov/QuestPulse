package commons.dto.response;

import java.util.List;

public record BookingDetailsResponse(
        String chatId,
        List<BookingResponse> bookedQuests
) {}
