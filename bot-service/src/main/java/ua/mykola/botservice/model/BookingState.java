package ua.mykola.botservice.model;

public class BookingState {
    private String questName;
    private String date;
    private String time;

    public BookingState(String questName) {
        this.questName = questName;
    }

    public String getQuestName() {
        return questName;
    }

    public void setQuestName(String questName) {
        this.questName = questName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
