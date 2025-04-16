package commons.dto.response;

import java.time.LocalTime;
import java.util.List;

public record AvailableTimesResponse(
        String chatId,
        List<LocalTime> availableTimes
) {}
