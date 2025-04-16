package ua.mykola.photoservice.messaging;

import commons.dto.RabbitQueues;
import commons.dto.response.PhotoChatIdResponse;
import commons.dto.response.PhotoNotificationResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ua.mykola.photoservice.service.S3Service;
import ua.mykola.photoservice.util.S3PathBuilder;

@Service
public class PhotoMessageListener {
    private final S3PathBuilder s3PathBuilder;
    private final S3Service s3Service;
    private final RabbitTemplate rabbitTemplate;

    public PhotoMessageListener(S3PathBuilder s3PathBuilder, S3Service s3Service, RabbitTemplate rabbitTemplate) {
        this.s3PathBuilder = s3PathBuilder;
        this.s3Service = s3Service;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitQueues.PHOTO_CHAT_ID_RESPONSE)
    public void handlePhotoChatIdResponse(PhotoChatIdResponse response) {
        String path = s3PathBuilder.buildPath(response.questCode(), response.date(), response.time(), response.fileType());
        byte[] photo = s3Service.download(path);

        rabbitTemplate.convertAndSend(RabbitQueues.PHOTO_NOTIFICATION_RESPONSE,
                new PhotoNotificationResponse(response.chatId(), photo, s3PathBuilder.getFileName(path))
        );
    }
}
