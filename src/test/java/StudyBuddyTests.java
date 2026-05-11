import manager.NotificationManager;
import model.Event;
import model.Reminder;
import model.Task;
import model.User;
import model.Calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudyBuddyTests {

    public static void main(String[] args) {
        testUserLogin();
        testCreateTaskRequiresLogin();
        testTaskCreation();
        testInvalidPriority();
        testMarkTaskComplete();
        testReminderDueTime();
        testNotificationManager();
        testEventCreationAndUpdate();
        testCalendarDailyView();

        System.out.println("All tests passed.");
    }

    private static void testUserLogin() {
        User user = new User("adam", "adam@email.com", "password123");

        assert !user.isLoggedIn();
        assert user.login("adam", "password123");
        assert user.isLoggedIn();

        user.logout();
        assert !user.isLoggedIn();
    }

    private static void testCreateTaskRequiresLogin() {
        User user = new User("adam", "adam@email.com", "password123");
        Task task = new Task("T1", "Study", "Study for exam", LocalDate.now(), 1);

        boolean exceptionThrown = false;

        try {
            user.createTask(task);
        } catch (IllegalStateException e) {
            exceptionThrown = true;
        }

        assert exceptionThrown;
    }

    private static void testTaskCreation() {
        Task task = new Task("T1", "Homework", "Finish assignment", LocalDate.now(), 2);

        assert task.getTaskID().equals("T1");
        assert task.getTitle().equals("Homework");
        assert task.getPriority() == 2;
        assert !task.isCompleted();
    }

    private static void testInvalidPriority() {
        boolean exceptionThrown = false;

        try {
            new Task("T2", "Bad Task", "Invalid priority", LocalDate.now(), 5);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }

        assert exceptionThrown;
    }

    private static void testMarkTaskComplete() {
        Task task = new Task("T3", "Quiz", "Complete quiz", LocalDate.now(), 1);

        task.markComplete();

        assert task.isCompleted();
    }

    private static void testReminderDueTime() {
        Reminder reminder = new Reminder("Submit assignment", LocalDateTime.now().minusMinutes(1));

        assert reminder.checkDueTime();
    }

    private static void testNotificationManager() {
        NotificationManager manager = new NotificationManager();
        Reminder reminder = new Reminder("Meeting reminder", LocalDateTime.now().minusMinutes(1));

        manager.scheduleNotification(reminder);
        int fired = manager.triggerReminder();

        assert fired == 1;
        assert reminder.isSent();
    }

    private static void testEventCreationAndUpdate() {
        Event event = new Event(1, "Meeting", "2026-05-10", "10:00", "11:00", "Library");

        assert event.getEventID() == 1;
        assert event.getTitle().equals("Meeting");
        assert event.getDate().equals("2026-05-10");
        assert event.getStartTime().equals("10:00");
        assert event.getEndTime().equals("11:00");
        assert event.getLocation().equals("Library");

        event.updateEvent("Updated Meeting", "2026-05-11", "12:00", "1:00", "Student Union");

        assert event.getTitle().equals("Updated Meeting");
        assert event.getDate().equals("2026-05-11");
        assert event.getStartTime().equals("12:00");
        assert event.getEndTime().equals("1:00");
        assert event.getLocation().equals("Student Union");
    }

    private static void testCalendarDailyView() {
        User user = new User("adam", "adam@email.com", "password123");
        user.login("adam", "password123");

        Task task = new Task("T4", "Project", "Finish Java project", LocalDate.of(2026, 5, 10), 1);
        Event event = new Event(2, "Study Session", "2026-05-10", "3:00", "4:00", "Library");

        user.createTask(task);
        user.createEvent(event);

        Calendar calendar = new Calendar(user);
        String dailyView = calendar.displayDailyView(LocalDate.of(2026, 5, 10));

        assert dailyView.contains("Project");
        assert dailyView.contains("Study Session");
    }
}