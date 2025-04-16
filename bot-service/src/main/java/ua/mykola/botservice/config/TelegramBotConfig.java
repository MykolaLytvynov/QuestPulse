package ua.mykola.botservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ua.mykola.botservice.bot.QuestPulseBot;

@Configuration
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(QuestPulseBot questPulseBot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(questPulseBot);
        return botsApi;
    }
}
