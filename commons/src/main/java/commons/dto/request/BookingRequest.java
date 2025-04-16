package commons.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookingRequest(
        String chatId,
        String questName,
        LocalDate date,
        LocalTime time
) {}
