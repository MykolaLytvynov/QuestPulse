package ua.mykola.questservice.mapper;

import commons.dto.response.QuestResponse;
import ua.mykola.questservice.entity.QuestEntity;

public class QuestMapper {

    public static QuestResponse toDto(QuestEntity entity) {
        return new QuestResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getPhone()
        );
    }
}
