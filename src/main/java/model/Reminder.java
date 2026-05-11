package model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Reminder {

    private final String reminderId;
    private String message;
    private LocalDateTime remindAt;
    private boolean sent;

    public Reminder(String message, LocalDateTime remindAt) {
        this(UUID.randomUUID().toString(), message, remindAt);
    }

    public Reminder(String reminderId, String message, LocalDateTime remindAt) {
        this.reminderId = (reminderId == null || reminderId.isBlank())
                ? UUID.randomUUID().toString()
                : reminderId;
        this.message = message;
        this.remindAt = remindAt;
        this.sent = false;
    }

    public String getReminderId() {
        return reminderId;
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

    public boolean isSent() {
        return sent;
    }

    public void markSent() {
        this.sent = true;
    }

    // Used by NotificationManager (Observer pattern) to decide when to fire.
    public boolean checkDueTime() {
        return checkDueTime(LocalDateTime.now());
    }

    public boolean checkDueTime(LocalDateTime now) {
        if (sent || remindAt == null || now == null) {
            return false;
        }
        return !now.isBefore(remindAt);
    }

    @Override
    public String toString() {
        return "Reminder{id=" + reminderId
                + ", message='" + message
                + "', remindAt=" + remindAt
                + ", sent=" + sent + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reminder other)) return false;
        return Objects.equals(reminderId, other.reminderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reminderId);
    }
}
