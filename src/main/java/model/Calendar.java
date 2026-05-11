package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Calendar aggregates a user's tasks and events and renders them for a
 * selected date in daily, weekly, or monthly form.
 *
 * Design notes:
 * - Encapsulation: task/event collections and the current view mode are private.
 *   Callers interact only through the display* and refresh methods.
 * - Single Responsibility: Calendar only filters and formats data for display.
 *   It does not create tasks/events, schedule notifications, or persist state.
 * - Composition: Calendar holds the User it belongs to so refreshCalendar()
 *   can pull the latest task/event lists from the source of truth.
 */
public class Calendar {

    public enum ViewMode { DAILY, WEEKLY, MONTHLY }

    private final User user;
    private final List<Task> taskCollection;
    private final List<Event> eventCollection;
    private ViewMode currentView;
    private LocalDate selectedDate;

    public Calendar(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Calendar requires a user.");
        }
        this.user = user;
        this.taskCollection = new ArrayList<>();
        this.eventCollection = new ArrayList<>();
        this.currentView = ViewMode.DAILY;
        this.selectedDate = LocalDate.now();
        refreshCalendar();
    }

    public ViewMode getCurrentView() {
        return currentView;
    }

    public void setCurrentView(ViewMode view) {
        if (view == null) {
            throw new IllegalArgumentException("View mode cannot be null.");
        }
        this.currentView = view;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Selected date cannot be null.");
        }
        this.selectedDate = date;
    }

    public List<Task> getTaskCollection() {
        return List.copyOf(taskCollection);
    }

    public List<Event> getEventCollection() {
        return List.copyOf(eventCollection);
    }

    // Pulls the latest tasks/events from the user so the calendar reflects
    // any creates/deletes that happened since the last render.
    public void refreshCalendar() {
        taskCollection.clear();
        eventCollection.clear();
        taskCollection.addAll(user.getTaskList());
        eventCollection.addAll(user.getEventList());
    }

    public String displayDailyView() {
        return displayDailyView(selectedDate);
    }

    public String displayDailyView(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null.");
        }
        refreshCalendar();
        List<Task> tasks = tasksFor(date);
        List<Event> events = eventsFor(date);

        StringBuilder sb = new StringBuilder();
        sb.append("Daily view - ").append(date).append("\n");
        appendTasks(sb, tasks);
        appendEvents(sb, events);
        return sb.toString();
    }

    public String displayWeeklyView() {
        return displayWeeklyView(selectedDate);
    }

    public String displayWeeklyView(LocalDate anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("Anchor date cannot be null.");
        }
        refreshCalendar();
        LocalDate weekStart = anchor.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        StringBuilder sb = new StringBuilder();
        sb.append("Weekly view - ").append(weekStart).append(" to ").append(weekEnd).append("\n");
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            sb.append("\n").append(day).append(" (").append(day.getDayOfWeek()).append(")\n");
            appendTasks(sb, tasksFor(day));
            appendEvents(sb, eventsFor(day));
        }
        return sb.toString();
    }

    public String displayMonthlyView() {
        return displayMonthlyView(selectedDate);
    }

    public String displayMonthlyView(LocalDate anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("Anchor date cannot be null.");
        }
        refreshCalendar();
        YearMonth month = YearMonth.from(anchor);

        StringBuilder sb = new StringBuilder();
        sb.append("Monthly view - ").append(month).append("\n");

        List<Task> monthTasks = tasksIn(month);
        List<Event> monthEvents = eventsIn(month);

        if (monthTasks.isEmpty() && monthEvents.isEmpty()) {
            sb.append("  (nothing scheduled this month)\n");
            return sb.toString();
        }

        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            List<Task> dayTasks = tasksFor(date);
            List<Event> dayEvents = eventsFor(date);
            if (dayTasks.isEmpty() && dayEvents.isEmpty()) {
                continue;
            }
            sb.append("\n").append(date).append("\n");
            appendTasks(sb, dayTasks);
            appendEvents(sb, dayEvents);
        }
        return sb.toString();
    }

    // Dispatches to the right view based on currentView, so a UI can just
    // change view mode and call render().
    public String render() {
        return switch (currentView) {
            case DAILY -> displayDailyView();
            case WEEKLY -> displayWeeklyView();
            case MONTHLY -> displayMonthlyView();
        };
    }

    // Returns the dates that the current view should render, in order.
    // DAILY = 1 date, WEEKLY = 7 dates (Mon..Sun), MONTHLY = every day of the month.
    public List<LocalDate> visibleDates() {
        return switch (currentView) {
            case DAILY -> List.of(selectedDate);
            case WEEKLY -> {
                LocalDate start = selectedDate.with(DayOfWeek.MONDAY);
                List<LocalDate> dates = new ArrayList<>(7);
                for (int i = 0; i < 7; i++) dates.add(start.plusDays(i));
                yield dates;
            }
            case MONTHLY -> {
                YearMonth month = YearMonth.from(selectedDate);
                List<LocalDate> dates = new ArrayList<>(month.lengthOfMonth());
                for (int day = 1; day <= month.lengthOfMonth(); day++) {
                    dates.add(month.atDay(day));
                }
                yield dates;
            }
        };
    }

    public List<Task> tasksFor(LocalDate date) {
        List<Task> matches = new ArrayList<>();
        for (Task t : taskCollection) {
            if (date.equals(t.getDeadline())) {
                matches.add(t);
            }
        }
        matches.sort(Comparator
                .comparingInt(Task::getPriority)
                .thenComparing(Task::getTitle, Comparator.nullsLast(String::compareTo)));
        return matches;
    }

    public List<Event> eventsFor(LocalDate date) {
        List<Event> matches = new ArrayList<>();
        for (Event e : eventCollection) {
            LocalDate eventDate = parseEventDate(e.getDate());
            if (date.equals(eventDate)) {
                matches.add(e);
            }
        }
        matches.sort(Comparator.comparing(Event::getStartTime,
                Comparator.nullsLast(String::compareTo)));
        return matches;
    }

    private List<Task> tasksIn(YearMonth month) {
        List<Task> matches = new ArrayList<>();
        for (Task t : taskCollection) {
            LocalDate d = t.getDeadline();
            if (d != null && YearMonth.from(d).equals(month)) {
                matches.add(t);
            }
        }
        return matches;
    }

    private List<Event> eventsIn(YearMonth month) {
        List<Event> matches = new ArrayList<>();
        for (Event e : eventCollection) {
            LocalDate d = parseEventDate(e.getDate());
            if (d != null && YearMonth.from(d).equals(month)) {
                matches.add(e);
            }
        }
        return matches;
    }

    // Event currently stores its date as a String; tolerate ISO format and
    // return null for anything else rather than throw, so an old/malformed
    // event doesn't break the whole view.
    private LocalDate parseEventDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    // Build the user-facing line ourselves from Task's getters rather than
    // calling Task.toString(), which is a developer-facing summary that
    // intentionally includes internal IDs for log readability.
    private void appendTasks(StringBuilder sb, List<Task> tasks) {
        if (tasks.isEmpty()) {
            sb.append("  No tasks for this day.\n");
            return;
        }
        sb.append("  Tasks:\n");
        for (Task t : tasks) {
            String title = (t.getTitle() == null || t.getTitle().isBlank())
                    ? "(Untitled Task)"
                    : t.getTitle();
            sb.append("    - ").append(title)
                    .append("  —  ").append(priorityWord(t.getPriority()))
                    .append(" priority, due ").append(t.getDeadline());
            if (t.isCompleted()) {
                sb.append("  [Done]");
            }
            sb.append("\n");
        }
    }

    private void appendEvents(StringBuilder sb, List<Event> events) {
        if (events.isEmpty()) {
            sb.append("  No events for this day.\n");
            return;
        }
        sb.append("  Events:\n");
        for (Event e : events) {
            String title = (e.getTitle() == null || e.getTitle().isBlank())
                    ? "(Untitled Event)"
                    : e.getTitle();
            sb.append("    - ")
                    .append(e.getStartTime()).append("–").append(e.getEndTime())
                    .append("  ").append(title);
            if (e.getLocation() != null && !e.getLocation().isBlank()) {
                sb.append("  @ ").append(e.getLocation());
            }
            sb.append("\n");
        }
    }

    private String priorityWord(int priority) {
        return switch (priority) {
            case 1 -> "High";
            case 2 -> "Medium";
            case 3 -> "Low";
            default -> "Unknown";
        };
    }
}
