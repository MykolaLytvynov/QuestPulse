package ua.mykola.photoservice.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Service
public class S3Service {
    private static final Logger LOG = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @PostConstruct
    private void checkBucket() {
        try {
            // Check if the bucket already exists
            s3Client.headBucket(builder -> builder.bucket(bucketName));
            LOG.debug("Bucket {} exists.", bucketName);
        } catch (S3Exception e) {
            // If not, attempt to create the bucket
            if (e.statusCode() == 404) {
                try {
                    s3Client.createBucket(builder -> builder.bucket(bucketName));
                    LOG.info("Bucket {} created successfully.", bucketName);
                } catch (S3Exception ex) {
                    LOG.error("Failed to create bucket {}: {}", bucketName, ex.awsErrorDetails().errorMessage());
                }
            }
        } catch (Exception e) {
            LOG.error("Unexpected error checking or creating bucket {}: {}", bucketName, e.getMessage());
        }
    }

    public String upload(MultipartFile file, String key) {

        try {
            // Create a request with metadata
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            // Upload the file to S3
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return key;
        } catch (IOException e) {
            LOG.error("Failed to upload file: {}", file, e);
            throw new RuntimeException("S3 upload error", e);
        }
    }

    public byte[] download(String key) {
        try {
            // Create a request to retrieve the object
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            // Download the object as bytes
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);

            return response.asByteArray();
        } catch (Exception e) {
            LOG.error("Failed to download file from S3 with key {}: {}", key, e.getMessage());
            throw new RuntimeException("S3 download error", e);
        }
    }
}
