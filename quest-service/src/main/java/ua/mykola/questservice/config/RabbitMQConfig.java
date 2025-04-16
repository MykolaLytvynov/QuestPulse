package ua.mykola.questservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import commons.dto.RabbitQueues;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter(objectMapper()));
        return rabbitTemplate;
    }

    @Bean
    public Queue questRequestQueue() {
        return new Queue(RabbitQueues.AVAILABLE_QUESTS_REQUEST, true);
    }

    @Bean
    public Queue timeRequestQueue() {
        return new Queue(RabbitQueues.AVAILABLE_TIMES_REQUEST, true);
    }

    @Bean
    public Queue bookingRequestQueue() {
        return new Queue(RabbitQueues.BOOKING_REQUEST, true);
    }

    @Bean
    public Queue detailsRequestQueue() {
        return new Queue(RabbitQueues.BOOKED_QUESTS_REQUEST, true);
    }

    @Bean
    public Queue photoChatIdQueue() {
        return new Queue(RabbitQueues.PHOTO_CHAT_ID_REQUEST, true);
    }
}
