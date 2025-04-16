package ua.mykola.photoservice.rest.controller;

import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.mykola.photoservice.rest.dto.UploadPhotoRequest;
import ua.mykola.photoservice.service.PhotoUploadService;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final PhotoUploadService photoUploadService;

    public UploadController(PhotoUploadService photoUploadService) {
        this.photoUploadService = photoUploadService;
    }

    @PostMapping
    public ResponseEntity<String> upload(@Valid @ModelAttribute UploadPhotoRequest request,
                                         BindingResult bindingResult,
                                         @RequestParam MultipartFile file) {
        if (bindingResult.hasErrors()) {
            String errorMessages = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Invalid input");

            return ResponseEntity.badRequest().body("Validation failed: " + errorMessages);
        }

        String path = photoUploadService.upload(request, file);
        return ResponseEntity.ok("Photo uploaded to path: " + path);
    }
}
