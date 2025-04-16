package ua.mykola.photoservice.service;

import commons.dto.RabbitQueues;
import commons.dto.request.PhotoChatIdRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.mykola.photoservice.rest.dto.UploadPhotoRequest;
import ua.mykola.photoservice.util.S3PathBuilder;

@Service
public class PhotoUploadService {
    private final S3PathBuilder s3PathBuilder;
    private final S3Service s3Service;
    private final RabbitTemplate rabbitTemplate;

    public PhotoUploadService(S3PathBuilder s3PathBuilder, S3Service s3Service, RabbitTemplate rabbitTemplate) {
        this.s3PathBuilder = s3PathBuilder;
        this.s3Service = s3Service;
        this.rabbitTemplate = rabbitTemplate;
    }

    public String upload(UploadPhotoRequest request, MultipartFile file) {
        String fileType = getFileType(file);
        String path = s3PathBuilder.buildPath(
                request.questCode(),
                request.date(),
                request.time(),
                fileType
        );

        s3Service.upload(file, path);

        PhotoChatIdRequest photoChatIdRequest = new PhotoChatIdRequest(request.questCode(), request.date(), request.time(), fileType);
        rabbitTemplate.convertAndSend(RabbitQueues.PHOTO_CHAT_ID_REQUEST, photoChatIdRequest);

        return path;
    }

    private String getFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }

        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
