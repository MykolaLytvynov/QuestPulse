package commons.dto.response;

import commons.dto.QuestCode;

import java.time.LocalDate;
import java.time.LocalTime;

public record PhotoChatIdResponse(
        String chatId,
        QuestCode questCode,
        LocalDate date,
        LocalTime time,
        String fileType
) {}
