package ua.mykola.questservice.messaging;

import commons.dto.RabbitQueues;
import commons.dto.request.AvailableTimeRequest;
import commons.dto.request.BookingRequest;
import commons.dto.request.PhotoChatIdRequest;
import commons.dto.request.QuestListRequest;
import commons.dto.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ua.mykola.questservice.entity.BookingEntity;
import ua.mykola.questservice.entity.QuestEntity;
import ua.mykola.questservice.mapper.BookingMapper;
import ua.mykola.questservice.property.BookingProperties;
import ua.mykola.questservice.repository.BookingRepository;
import ua.mykola.questservice.repository.QuestRepository;
import ua.mykola.questservice.mapper.QuestMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingListener {
    private static final Logger LOG = LoggerFactory.getLogger(BookingListener.class);
    private final RabbitTemplate rabbitTemplate;
    private final QuestRepository questRepository;
    private final BookingProperties bookingProperties;
    private final BookingRepository bookingRepository;

    public BookingListener(RabbitTemplate rabbitTemplate,
                           QuestRepository questRepository,
                           BookingProperties bookingProperties,
                           BookingRepository bookingRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.questRepository = questRepository;
        this.bookingProperties = bookingProperties;
        this.bookingRepository = bookingRepository;
    }

    @RabbitListener(queues = RabbitQueues.AVAILABLE_QUESTS_REQUEST)
    public void handleQuestListRequest(QuestListRequest request) {
        List<QuestResponse> quests = questRepository.findAll().stream()
                .map(QuestMapper::toDto)
                .toList();

        QuestListResponse response = new QuestListResponse(
                request.chatId(),
                quests
        );

        rabbitTemplate.convertAndSend(RabbitQueues.AVAILABLE_QUESTS_RESPONSE, response);
    }

    @RabbitListener(queues = RabbitQueues.AVAILABLE_TIMES_REQUEST)
    public void handleAvailableTimeRequest(AvailableTimeRequest request) {
        List<LocalTime> bookedSlots = bookingRepository.findBookedTimesByQuestNameAndDate(request.questName(), request.date());
        List<LocalTime> availableSlots = bookingProperties.getAvailableSlots().stream()
                .filter(time -> !bookedSlots.contains(time))
                .toList();
        AvailableTimesResponse response = new AvailableTimesResponse(
                request.chatId(),
                availableSlots
        );
        rabbitTemplate.convertAndSend(RabbitQueues.AVAILABLE_TIMES_RESPONSE, response);
    }

    @RabbitListener(queues = RabbitQueues.BOOKING_REQUEST)
    public void handleBookingRequest(BookingRequest request) {
        Optional<QuestEntity> quest = questRepository.findByName(request.questName());
        if (quest.isPresent()) {
            BookingEntity newBookedQuest = BookingMapper.toEntity(request, quest.get());
            bookingRepository.save(newBookedQuest);

            BookingResponse response = BookingMapper.toResponse(newBookedQuest);
            rabbitTemplate.convertAndSend(RabbitQueues.BOOKING_RESPONSE, response);
        } else {
            LOG.warn("Quest was not found by request {}.", request);
        }
    }

    @RabbitListener(queues = RabbitQueues.BOOKED_QUESTS_REQUEST)
    public void handleBookedQuestsRequest(String chatId) {
        Sort sort = Sort.by(Sort.Order.asc("date"), Sort.Order.asc("time"));
        List<BookingEntity> bookedQuests = bookingRepository
                .findByChatIdAndDateGreaterThanEqual(chatId, LocalDate.now(), sort);

        BookingDetailsResponse response = new BookingDetailsResponse(
                chatId,
                bookedQuests.stream()
                        .map(BookingMapper::toResponse)
                        .toList());
        rabbitTemplate.convertAndSend(RabbitQueues.BOOKED_QUESTS_RESPONSE, response);
    }

    @RabbitListener(queues = RabbitQueues.PHOTO_CHAT_ID_REQUEST)
    public void handlePhotoChatIdRequest(PhotoChatIdRequest request) {
        Optional<String> chatId = bookingRepository.findChatIdByQuestCodeDateTime(request.questCode(), request.date(), request.time());
        if (chatId.isPresent()) {
            PhotoChatIdResponse response = new PhotoChatIdResponse(
                    chatId.get(),
                    request.questCode(),
                    request.date(),
                    request.time(),
                    request.fileType()
            );
            rabbitTemplate.convertAndSend(RabbitQueues.PHOTO_CHAT_ID_RESPONSE, response);
        } else {
            LOG.warn("Chat id was not found by request {}.", request);
        }
    }
}
