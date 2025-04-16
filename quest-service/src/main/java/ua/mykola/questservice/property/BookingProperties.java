package ua.mykola.questservice.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "booking")
public class BookingProperties {
    private List<String> availableSlots;
    private List<LocalTime> parsedAvailableSlots;

    public List<LocalTime> getAvailableSlots() {
        return parsedAvailableSlots;
    }

    public void setAvailableSlots(List<String> availableSlots) {
        this.availableSlots = availableSlots;
        parsedAvailableSlots = availableSlots.stream()
                .map(LocalTime::parse)
                .sorted()
                .toList();
    }
}
