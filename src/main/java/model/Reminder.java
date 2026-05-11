package model;

import java.time.LocalDateTime;

public class Reminder {

    private String message;
    private LocalDateTime remindAt;

    public Reminder(String message, LocalDateTime remindAt) {
        this.message = message;
        this.remindAt = remindAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(LocalDateTime remindAt) {
        this.remindAt = remindAt;
    }

    @Override
    public String toString() {
        return "Reminder{message='" + message + "', remindAt=" + remindAt + "}";
    }
}