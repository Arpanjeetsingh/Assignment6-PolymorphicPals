package manager;

import model.Reminder;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * NotificationManager is the central controller for reminders.
 *
 * Implements the Observer pattern: reminders sit in activeReminders, and the
 * manager polls them via triggerReminder(). When a reminder's checkDueTime()
 * returns true, the manager moves it to notificationQueue and dispatches it.
 *
 * Tasks/events stay unaware of how notifications are delivered — they just
 * register a Reminder here and the manager handles the rest. The Consumer
 * "listener" lets the UI hook in (e.g. show a popup) without coupling this
 * class to JavaFX.
 */
public class NotificationManager {

    private final List<Reminder> activeReminders;
    private final Queue<Reminder> notificationQueue;
    private Consumer<Reminder> listener;

    public NotificationManager() {
        this.activeReminders = new ArrayList<>();
        this.notificationQueue = new ArrayDeque<>();
    }

    // The UI (or a test) sets this to receive sendNotification callbacks.
    public void setListener(Consumer<Reminder> listener) {
        this.listener = listener;
    }

    public List<Reminder> getActiveReminders() {
        return List.copyOf(activeReminders);
    }

    public Queue<Reminder> getNotificationQueue() {
        return new ArrayDeque<>(notificationQueue);
    }

    // Adds a reminder to be watched. Same reminder is not added twice.
    public void registerReminder(Reminder reminder) {
        if (reminder == null) {
            throw new IllegalArgumentException("Reminder cannot be null.");
        }
        if (!activeReminders.contains(reminder)) {
            activeReminders.add(reminder);
        }
    }

    // Convenience: register and immediately attempt to schedule. If it is
    // already past due, it will be picked up on the next triggerReminder().
    public void scheduleNotification(Reminder reminder) {
        registerReminder(reminder);
    }

    public boolean cancelNotification(Reminder reminder) {
        if (reminder == null) return false;
        notificationQueue.remove(reminder);
        return activeReminders.remove(reminder);
    }

    // Polls every active reminder. Anything due gets queued and dispatched.
    // Called by a periodic timer (see the JavaFX integration) or manually
    // in tests with a fixed "now".
    public int triggerReminder() {
        return triggerReminder(LocalDateTime.now());
    }

    public int triggerReminder(LocalDateTime now) {
        int fired = 0;
        Iterator<Reminder> it = activeReminders.iterator();
        while (it.hasNext()) {
            Reminder r = it.next();
            if (r.checkDueTime(now)) {
                notificationQueue.offer(r);
                it.remove();
                fired++;
            }
        }
        drainQueue();
        return fired;
    }

    // sendNotification handles the actual delivery for one reminder. Kept
    // public so a caller can force-send (e.g. a "Send now" button) without
    // waiting for the due time.
    public void sendNotification(Reminder reminder) {
        if (reminder == null) return;
        reminder.markSent();
        // Always log to stderr so the firing is observable in the console
        // even if the UI listener swallows or hides the popup.
        System.err.println("[NotificationManager] firing reminder: \""
                + reminder.getMessage() + "\" (scheduled for " + reminder.getRemindAt() + ")");
        if (listener != null) {
            listener.accept(reminder);
        }
    }

    private void drainQueue() {
        while (!notificationQueue.isEmpty()) {
            sendNotification(notificationQueue.poll());
        }
    }
}
