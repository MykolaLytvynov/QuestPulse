package commons.dto.response;

public record QuestResponse(
        Long id,
        String name,
        String description,
        String address,
        String phone
) {}
