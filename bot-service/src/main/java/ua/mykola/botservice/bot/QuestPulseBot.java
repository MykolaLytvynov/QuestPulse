package ua.mykola.botservice.bot;

import commons.dto.RabbitQueues;
import commons.dto.request.AvailableTimeRequest;
import commons.dto.request.BookingRequest;
import commons.dto.request.QuestListRequest;
import commons.dto.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.mykola.botservice.model.BookingState;
import ua.mykola.botservice.model.BotCommand;
import ua.mykola.botservice.model.CallbackData;
import ua.mykola.botservice.model.CallbackType;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QuestPulseBot extends TelegramLongPollingBot {
    private static final Logger LOG = LoggerFactory.getLogger(QuestPulseBot.class);

    private static final Integer TWO_WEEKS = 14;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final Map<String, BookingState> bookings = new ConcurrentHashMap<>();

    private final RabbitTemplate rabbitTemplate;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    public QuestPulseBot(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String message = update.getMessage().getText().trim();

            BotCommand command = BotCommand.fromString(message);

            if (command != null) {
                processCommand(chatId, command);
            }
        } else {
            if (update.hasCallbackQuery()) {
                String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
                String callbackData = update.getCallbackQuery().getData();
                processCallback(chatId, callbackData);
            }
        }
    }

    private void processCommand(String chatId, BotCommand command) {
        switch (command) {
            case START -> {
                sendTextMessage(chatId, "Welcome! I can help you book a quest.");
                showMainMenu(chatId);
            }
            case BOOK -> requestAvailableQuests(chatId);
            case DETAILS_MY_BOOKS -> requestBookedQuests(chatId);
        }
    }

    private void processCallback(String chatId, String data) {
        CallbackData callbackData = CallbackData.from(data);

        switch (callbackData.type()) {
            case QUEST -> {
                bookings.put(chatId, new BookingState(callbackData.value()));
                sendTextMessage(chatId, "You have selected: " + callbackData.value());
                showAvailableDates(chatId);
            }
            case DATE -> {
                if (!bookings.containsKey(chatId)) {
                    requestAvailableQuests(chatId);
                    return;
                }
                bookings.get(chatId).setDate(callbackData.value());
                requestAvailableTimes(chatId);
            }
            case TIME -> {
                if (!bookings.containsKey(chatId)) {
                    requestAvailableQuests(chatId);
                    return;
                }
                bookings.get(chatId).setTime(callbackData.value());
                requestBooking(chatId);
            }
        }
    }

    private void requestBookedQuests(String chatId) {
        sendTextMessage(chatId, "Fetching your booked quests...");
        rabbitTemplate.convertAndSend(RabbitQueues.BOOKED_QUESTS_REQUEST, chatId);
    }

    private void requestBooking(String chatId) {
        BookingState bookingState = bookings.get(chatId);
        BookingRequest request = new BookingRequest(
                chatId,
                bookingState.getQuestName(),
                LocalDate.parse(bookingState.getDate(), formatter),
                LocalTime.parse(bookingState.getTime())
        );
        rabbitTemplate.convertAndSend(RabbitQueues.BOOKING_REQUEST, request);

        bookings.remove(chatId);
    }

    private void requestAvailableQuests(String chatId) {
        sendTextMessage(chatId, "Please choose a quest:");

        QuestListRequest request = new QuestListRequest(chatId);
        rabbitTemplate.convertAndSend(RabbitQueues.AVAILABLE_QUESTS_REQUEST, request);
    }

    private void showAvailableDates(String chatId) {
        SendMessage message = new SendMessage(chatId, "Please choose a date for your quest:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<String> dates = fetchUpcomingDates();

        // Create rows with two dates in each
        for (int i = 0; i < dates.size(); i += 2) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            // Add the first button
            InlineKeyboardButton button1 = buildInlineButton(CallbackType.DATE, dates.get(i));
            row.add(button1);

            // Add the second button if it exists
            if (i + 1 < dates.size()) {
                InlineKeyboardButton button2 = buildInlineButton(CallbackType.DATE, dates.get(i + 1));
                row.add(button2);
            }

            // Add the row of buttons to the list
            rows.add(row);
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        message.setReplyMarkup(inlineKeyboardMarkup);

        safelyExecute(message);
    }

    private void requestAvailableTimes(String chatId) {
        String selectedDate = bookings.get(chatId).getDate();
        String selectedQuestName = bookings.get(chatId).getQuestName();

        sendTextMessage(chatId, "Fetching available times for " + selectedDate + "...");

        AvailableTimeRequest request = new AvailableTimeRequest(chatId,
                selectedQuestName,
                LocalDate.parse(selectedDate, formatter)
        );
        rabbitTemplate.convertAndSend(RabbitQueues.AVAILABLE_TIMES_REQUEST, request);
    }

    @RabbitListener(queues = RabbitQueues.AVAILABLE_QUESTS_RESPONSE)
    private void handleAvailableQuestsResponse(QuestListResponse response) {
        if (response.quests().isEmpty()) {
            sendTextMessage(response.chatId(), "No quests found.");
            return;
        }
        String chatId = response.chatId();

        SendMessage message = new SendMessage(chatId, "Available quests:");
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        response.quests().forEach(quest -> {
            InlineKeyboardButton button = buildInlineButton(CallbackType.QUEST, quest.name());
            rows.add(Collections.singletonList(button));
        });

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rows);

        message.setReplyMarkup(inlineKeyboardMarkup);

        safelyExecute(message);
    }

    @RabbitListener(queues = RabbitQueues.AVAILABLE_TIMES_RESPONSE)
    private void handleAvailableTimesResponse(AvailableTimesResponse response) {
        SendMessage message = new SendMessage(response.chatId(),  "Now, please select a time:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        response.availableTimes().forEach(time -> {
            InlineKeyboardButton button = buildInlineButton(CallbackType.TIME, time.toString());
            rows.add(Collections.singletonList(button));
        });

        inlineKeyboardMarkup.setKeyboard(rows);
        message.setReplyMarkup(inlineKeyboardMarkup);

        safelyExecute(message);
    }

    @RabbitListener(queues = RabbitQueues.BOOKING_RESPONSE)
    private void handleBookedQuestResponse(BookingResponse response) {
        String text = """
            ✅ Booking Confirmed!

            🧩 Quest: %s
            📍 Location: %s
            📅 Date: %s
            ⏰ Time: %s

            📄 Description: %s
            """.formatted(
                response.questName(),
                response.questLocation(),
                response.date().format(formatter),
                response.time(),
                response.questDescription()
        );
        SendMessage message = new SendMessage(response.chatId(), text);

        safelyExecute(message);
    }

    @RabbitListener(queues = RabbitQueues.BOOKED_QUESTS_RESPONSE)
    private void handleBookedQuestsResponse(BookingDetailsResponse response) {
        if (response.bookedQuests().isEmpty()) {
            sendTextMessage(response.chatId(), "No booked quests found.");
            return;
        }

        StringBuilder text = new StringBuilder("Your quests are as follows:\n\n");

        for (int i = 0; i < response.bookedQuests().size(); i++) {
            BookingResponse bookedQuest = response.bookedQuests().get(i);
            text.append(String.format("""
                %d.
                🧩 Quest: %s
                📍 Location: %s
                📅 Date: %s
                ⏰ Time: %s

                📄 Description: %s
                ----------------------
                """,
                    i + 1,
                    bookedQuest.questName(),
                    bookedQuest.questLocation(),
                    bookedQuest.date().format(formatter),
                    bookedQuest.time(),
                    bookedQuest.questDescription()
            ));
        }

        SendMessage message = new SendMessage(response.chatId(), text.toString());
        safelyExecute(message);
    }

    @RabbitListener(queues = RabbitQueues.PHOTO_NOTIFICATION_RESPONSE)
    private void handlePhotoNotificationResponse(PhotoNotificationResponse response) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(response.chatId());
        sendPhoto.setPhoto(new InputFile(new ByteArrayInputStream(response.photoBytes()), response.fileName()));

        sendTextMessage(response.chatId(), """
            Thank you for choosing our quests rooms! 🎉
            Here is your photo 📸
            We hope to see you again soon!
            """);

        safelyExecute(sendPhoto);
    }

    private List<String> fetchUpcomingDates() {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < TWO_WEEKS; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            dates.add(dateFormat.format(calendar.getTime()));
        }
        return dates;
    }

    private void showMainMenu(String chatId) {
        SendMessage message = new SendMessage(chatId, "Choose an option:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = Arrays.stream(BotCommand.values())
                .map(cmd -> {
                    KeyboardRow row = new KeyboardRow();
                    row.add(new KeyboardButton(cmd.getCommand()));
                    return row;
                })
                .toList();

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        safelyExecute(message);
    }

    private InlineKeyboardButton buildInlineButton(CallbackType callbackType, String callbackValue) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(callbackValue);

        CallbackData callbackData = new CallbackData(callbackType, callbackValue);
        button.setCallbackData(callbackData.toString());

        return button;
    }

    private void sendTextMessage(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        safelyExecute(message);
    }

    private void safelyExecute(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOG.error("Failed to send message: {}", message, e);
        }
    }
    private void safelyExecute(SendPhoto photo) {
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            LOG.error("Failed to send photo: {}", photo, e);
        }
    }
}
