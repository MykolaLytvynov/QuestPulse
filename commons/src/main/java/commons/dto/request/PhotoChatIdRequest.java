package commons.dto.request;

import commons.dto.QuestCode;

import java.time.LocalDate;
import java.time.LocalTime;

public record PhotoChatIdRequest(
        QuestCode questCode,
        LocalDate date,
        LocalTime time,
        String fileType
) {}
