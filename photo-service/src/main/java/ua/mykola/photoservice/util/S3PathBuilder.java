package ua.mykola.photoservice.util;

import commons.dto.QuestCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class S3PathBuilder {

    public String buildPath(QuestCode code, LocalDate date, LocalTime time, String fileType) {
        return "%s/%s/%s.%s".formatted(code, date, time, fileType);
    }

    public String getFileName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
