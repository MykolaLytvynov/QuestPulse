package ua.mykola.questservice.mapper;

import commons.dto.request.BookingRequest;
import commons.dto.response.BookingResponse;
import ua.mykola.questservice.entity.BookingEntity;
import ua.mykola.questservice.entity.QuestEntity;

public class BookingMapper {

    public static BookingEntity toEntity(BookingRequest bookingRequest, QuestEntity questEntity) {
        return new BookingEntity(
                bookingRequest.chatId(),
                questEntity,
                bookingRequest.date(),
                bookingRequest.time()
        );
    }

    public static BookingResponse toResponse(BookingEntity bookingEntity) {
        return new BookingResponse(
                bookingEntity.getChatId(),
                bookingEntity.getQuest().getName(),
                bookingEntity.getQuest().getDescription(),
                bookingEntity.getQuest().getAddress(),
                bookingEntity.getDate(),
                bookingEntity.getTime()
        );
    }
}
