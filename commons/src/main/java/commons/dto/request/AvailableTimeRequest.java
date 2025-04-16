package commons.dto.request;

import java.time.LocalDate;

public record AvailableTimeRequest(
        String chatId,
        String questName,
        LocalDate date
) {}
