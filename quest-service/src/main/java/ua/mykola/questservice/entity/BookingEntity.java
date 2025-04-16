package ua.mykola.questservice.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "bookings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"quest_id", "date", "time"})
})
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private String chatId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "quest_id", nullable = false)
    private QuestEntity quest;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    public BookingEntity() {
    }

    public BookingEntity(String chatId, QuestEntity quest, LocalDate date, LocalTime time) {
        this.chatId = chatId;
        this.quest = quest;
        this.date = date;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public QuestEntity getQuest() {
        return quest;
    }

    public void setQuest(QuestEntity quest) {
        this.quest = quest;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
