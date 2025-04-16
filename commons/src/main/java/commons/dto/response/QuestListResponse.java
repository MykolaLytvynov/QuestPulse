package commons.dto.response;

import java.util.List;

public record QuestListResponse(
        String chatId,
        List<QuestResponse> quests
) {}
