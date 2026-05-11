package model;

import java.time.LocalDate;
import java.util.Objects;

public class Task {

    private String taskID;
    private String title;
    private String description;
    private LocalDate deadline;
    private int priority;   // (ex: 1 = high, 2 = medium, 3 = low)
    private boolean completed;
    private Reminder reminder;

    public Task(String taskID, String title, String description, LocalDate deadline, int priority) {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline cannot be null");
        }
        this.taskID = taskID;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        setPriority(priority);
        this.completed = false;
    }

    public String getTaskID() {
        return taskID;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setDeadline(LocalDate deadline) {
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline cannot be null");
        }
        this.deadline = deadline;
    }

    public void setPriority(int priority) {
        if (priority < 1 || priority > 3) {
            throw new IllegalArgumentException("Priority must be 1, 2, or 3");
        }
        this.priority = priority;
    }

    public void markComplete() {
        this.completed = true;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }

    public void removeReminder() {
        this.reminder = null;
    }

    @Override
    public String toString() {
        String priorityLabel = switch (priority) {
            case 1 -> "High";
            case 2 -> "Medium";
            case 3 -> "Low";
            default -> String.valueOf(priority);
        };

        return (title == null || title.isBlank() ? "(Untitled Task)" : title)
                + " [ID=" + taskID + "]"
                + " [Due=" + deadline + "]"
                + " [Priority=" + priorityLabel + "]"
                + (completed ? " [Completed]" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;
        return Objects.equals(taskID, task.taskID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskID);
    }
}