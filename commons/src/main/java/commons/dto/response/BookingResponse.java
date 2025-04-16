package commons.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookingResponse(
        String chatId,
        String questName,
        String questDescription,
        String questLocation,
        LocalDate date,
        LocalTime time
) {}
