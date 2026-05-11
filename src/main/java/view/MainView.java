package view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import manager.NotificationManager;
import model.Calendar;
import model.Event;
import model.Reminder;
import model.Task;
import model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainView {
    private Stage stage;

    public MainView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        Label title = new Label("Assignment 6 - Polymorphic Pals");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Demo user so Calendar (which is owned by a User) has somewhere to read from.
        User user = new User("demo", "demo@studybuddy.local", "demo123");
        user.login("demo", "demo123");

        Calendar calendar = new Calendar(user);
        NotificationManager notifications = new NotificationManager();

        ObservableList<Task> tasks = FXCollections.observableArrayList();
        AtomicInteger nextTaskNum = new AtomicInteger(1);

        ListView<Task> taskList = new ListView<>(tasks);
        taskList.setPrefWidth(320);

        TextField titleField = new TextField();
        titleField.setPromptText("Task title");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Task description");

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Deadline");

        ChoiceBox<String> priorityChoice = new ChoiceBox<>(FXCollections.observableArrayList("High", "Medium", "Low"));
        priorityChoice.setValue("Medium");

        Label selectedTaskLabel = new Label("Selected: (none)");

        TextField reminderMessageField = new TextField();
        reminderMessageField.setPromptText("Reminder message");

        DatePicker reminderDatePicker = new DatePicker();
        reminderDatePicker.setPromptText("Reminder date");

        TextField reminderTimeField = new TextField();
        reminderTimeField.setPromptText("e.g. 5:30 PM or 17:30");

        Label reminderStatusLabel = new Label("");
        reminderStatusLabel.setStyle("-fx-font-size: 11px;");

        Button saveTaskBtn = new Button("Save Task");
        Button updateTaskBtn = new Button("Update Task");
        Button markCompleteBtn = new Button("Mark Complete");
        Button deleteTaskBtn = new Button("Delete");
        Button addReminderBtn = new Button("Add Reminder");
        Button removeReminderBtn = new Button("Remove Reminder");

        saveTaskBtn.setOnAction(e -> {
            try {
                String id = "T-" + nextTaskNum.getAndIncrement();
                String t = titleField.getText();
                String desc = descriptionField.getText();
                LocalDate deadline = deadlinePicker.getValue();
                if (deadline == null) {
                    showError("Could not save task", "Deadline cannot be null");
                    return;
                }
                int priority = toPriorityValue(priorityChoice.getValue());

                Task task = new Task(id, t, desc, deadline, priority);
                tasks.add(task);
                user.createTask(task);
                taskList.getSelectionModel().select(task);
            } catch (IllegalArgumentException ex) {
                showError("Could not save task", ex.getMessage());
            }
        });

        updateTaskBtn.setOnAction(e -> {
            Task selected = taskList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("No selection", "Select a task first.");
                return;
            }
            try {
                selected.setDeadline(deadlinePicker.getValue());
                selected.setPriority(toPriorityValue(priorityChoice.getValue()));
                tasks.set(tasks.indexOf(selected), selected);
            } catch (IllegalArgumentException ex) {
                showError("Could not update task", ex.getMessage());
            }
        });

        markCompleteBtn.setOnAction(e -> {
            Task selected = taskList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("No selection", "Select a task first.");
                return;
            }
            selected.markComplete();
            tasks.set(tasks.indexOf(selected), selected);
        });

        deleteTaskBtn.setOnAction(e -> {
            Task selected = taskList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("No selection", "Select a task first.");
                return;
            }
            Reminder attached = selected.getReminder();
            if (attached != null) {
                notifications.cancelNotification(attached);
            }
            tasks.remove(selected);
            user.deleteTask(selected);
        });

        addReminderBtn.setOnAction(e -> {
            Task selected = taskList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("No selection", "Select a task first.");
                return;
            }

            LocalDate date = reminderDatePicker.getValue();
            LocalTime time = parseTime(reminderTimeField.getText());
            if (date == null || time == null) {
                showError("Invalid reminder time", "Pick a reminder date and time (HH:mm).");
                return;
            }

            Reminder previous = selected.getReminder();
            if (previous != null) {
                notifications.cancelNotification(previous);
            }

            String msg = reminderMessageField.getText();
            Reminder reminder = new Reminder(msg, LocalDateTime.of(date, time));
            selected.setReminder(reminder);
            notifications.scheduleNotification(reminder);
            tasks.set(tasks.indexOf(selected), selected);
            reminderStatusLabel.setText("✓ Reminder set for " + date + " " + time);
            reminderStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60;");
        });

        removeReminderBtn.setOnAction(e -> {
            Task selected = taskList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("No selection", "Select a task first.");
                return;
            }
            Reminder attached = selected.getReminder();
            if (attached != null) {
                notifications.cancelNotification(attached);
            }
            selected.removeReminder();
            tasks.set(tasks.indexOf(selected), selected);
            reminderStatusLabel.setText("Reminder removed.");
            reminderStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        });

        taskList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                selectedTaskLabel.setText("Selected: (none)");
                return;
            }
            selectedTaskLabel.setText("Selected: " + newV.toString());
            titleField.setText(newV.getTitle());
            descriptionField.setText(newV.getDescription());
            deadlinePicker.setValue(newV.getDeadline());
            priorityChoice.setValue(toPriorityLabel(newV.getPriority()));

            Reminder r = newV.getReminder();
            if (r != null) {
                reminderMessageField.setText(r.getMessage());
                if (r.getRemindAt() != null) {
                    reminderDatePicker.setValue(r.getRemindAt().toLocalDate());
                    reminderTimeField.setText(r.getRemindAt().toLocalTime().toString());
                }
                if (r.isSent()) {
                    reminderStatusLabel.setText("Reminder for " + r.getRemindAt() + " already fired.");
                    reminderStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
                } else {
                    reminderStatusLabel.setText("Reminder scheduled for " + r.getRemindAt() + ".");
                    reminderStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60;");
                }
            } else {
                reminderMessageField.clear();
                reminderDatePicker.setValue(null);
                reminderTimeField.clear();
                reminderStatusLabel.setText("");
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(12));

        int row = 0;
        form.add(new Label("Title"), 0, row);
        form.add(titleField, 1, row++);

        form.add(new Label("Description"), 0, row);
        form.add(descriptionField, 1, row++);

        form.add(new Label("Deadline"), 0, row);
        form.add(deadlinePicker, 1, row++);

        form.add(new Label("Priority"), 0, row);
        form.add(priorityChoice, 1, row++);

        form.add(new Separator(), 0, row++, 2, 1);

        form.add(new Label("Reminder message"), 0, row);
        form.add(reminderMessageField, 1, row++);

        HBox reminderTimeRow = new HBox(10, reminderDatePicker, reminderTimeField);
        form.add(new Label("Reminder at"), 0, row);
        form.add(reminderTimeRow, 1, row++);

        ButtonBar taskButtons = new ButtonBar();
        taskButtons.getButtons().addAll(saveTaskBtn, updateTaskBtn, deleteTaskBtn);

        ButtonBar otherButtons = new ButtonBar();
        otherButtons.getButtons().addAll(addReminderBtn, removeReminderBtn, markCompleteBtn);

        VBox right = new VBox(10, selectedTaskLabel, form, taskButtons, otherButtons, reminderStatusLabel);
        right.setPadding(new Insets(12));

        VBox left = new VBox(10, new Label("Tasks"), taskList);
        left.setPadding(new Insets(12));

        HBox tasksContent = new HBox(12, left, right);
        HBox.setHgrow(right, Priority.ALWAYS);

        // ===== Calendar tab =====
        DatePicker calendarDatePicker = new DatePicker(LocalDate.now());
        Button todayBtn = new Button("Today");
        Button dailyBtn = new Button("Daily");
        Button weeklyBtn = new Button("Weekly");
        Button monthlyBtn = new Button("Monthly");
        Label calendarStatus = new Label("View: Daily");
        calendarStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        VBox calendarBody = new VBox(8);
        calendarBody.setPadding(new Insets(6));
        ScrollPane calendarScroll = new ScrollPane(calendarBody);
        calendarScroll.setFitToWidth(true);
        calendarScroll.setStyle("-fx-background: white; -fx-background-color: white;");
        VBox.setVgrow(calendarScroll, Priority.ALWAYS);

        final String activeViewStyle = "-fx-font-weight: bold; -fx-base: #4a90e2; -fx-text-fill: white;";
        final String inactiveViewStyle = "";
        Runnable highlightActiveView = () -> {
            dailyBtn.setStyle(inactiveViewStyle);
            weeklyBtn.setStyle(inactiveViewStyle);
            monthlyBtn.setStyle(inactiveViewStyle);
            switch (calendar.getCurrentView()) {
                case DAILY -> dailyBtn.setStyle(activeViewStyle);
                case WEEKLY -> weeklyBtn.setStyle(activeViewStyle);
                case MONTHLY -> monthlyBtn.setStyle(activeViewStyle);
            }
        };

        Runnable renderCalendar = () -> {
            LocalDate picked = calendarDatePicker.getValue();
            if (picked != null) {
                calendar.setSelectedDate(picked);
            }
            calendar.refreshCalendar();
            calendarBody.getChildren().clear();

            boolean isMonthly = calendar.getCurrentView() == Calendar.ViewMode.MONTHLY;
            boolean anyContent = false;
            for (LocalDate day : calendar.visibleDates()) {
                List<Task> dayTasks = calendar.tasksFor(day);
                List<Event> dayEvents = calendar.eventsFor(day);
                if (isMonthly && dayTasks.isEmpty() && dayEvents.isEmpty()) {
                    continue;
                }
                anyContent = true;
                calendarBody.getChildren().add(buildDayCard(day, dayTasks, dayEvents));
            }
            if (!anyContent) {
                Label empty = new Label("Nothing scheduled in this view.");
                empty.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                calendarBody.getChildren().add(empty);
            }

            calendarStatus.setText("View: " + capitalize(calendar.getCurrentView().name()));
            highlightActiveView.run();
        };

        dailyBtn.setOnAction(e -> {
            calendar.setCurrentView(Calendar.ViewMode.DAILY);
            renderCalendar.run();
        });
        weeklyBtn.setOnAction(e -> {
            calendar.setCurrentView(Calendar.ViewMode.WEEKLY);
            renderCalendar.run();
        });
        monthlyBtn.setOnAction(e -> {
            calendar.setCurrentView(Calendar.ViewMode.MONTHLY);
            renderCalendar.run();
        });
        todayBtn.setOnAction(e -> {
            calendarDatePicker.setValue(LocalDate.now());
            renderCalendar.run();
        });
        calendarDatePicker.valueProperty().addListener((obs, o, n) -> renderCalendar.run());

        // Re-render the calendar whenever the task list changes so newly
        // saved/deleted tasks show up automatically.
        tasks.addListener((javafx.collections.ListChangeListener<Task>) c -> renderCalendar.run());

        HBox calendarControls = new HBox(10, new Label("Date:"), calendarDatePicker, todayBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                dailyBtn, weeklyBtn, monthlyBtn);
        calendarControls.setPadding(new Insets(4, 0, 4, 0));
        VBox calendarBox = new VBox(10, calendarControls, calendarStatus, calendarScroll);
        calendarBox.setPadding(new Insets(12));

        renderCalendar.run();

        // Wire NotificationManager to surface popups on the JavaFX thread.
        // Use show() (non-blocking) + initOwner(stage) so the alert reliably
        // appears in front of the main window instead of behind it.
        //
        // The Reminder class (per the UML) has no back-reference to Task, so
        // we look up which task owns the firing reminder by scanning the
        // user's task list. This keeps the popup useful (shows task title)
        // without polluting the Reminder model with a taskId field.
        notifications.setListener(r -> Platform.runLater(() -> {
            String taskTitle = null;
            for (Task t : user.getTaskList()) {
                if (r.equals(t.getReminder())) {
                    taskTitle = (t.getTitle() == null || t.getTitle().isBlank())
                            ? "(Untitled Task)"
                            : t.getTitle();
                    break;
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(stage);
            alert.setTitle("Reminder");
            alert.setHeaderText(taskTitle != null ? "Reminder: " + taskTitle : "Reminder due now");

            String message = (r.getMessage() == null) ? "" : r.getMessage().trim();
            String scheduled = "Scheduled for " + r.getRemindAt();
            alert.setContentText(message.isEmpty() ? scheduled : message + "\n\n" + scheduled);
            alert.show();
            stage.toFront();
        }));

        // Poll for due reminders every 2 seconds. The Observer pattern lives
        // here: the manager watches its activeReminders and fires when due.
        Timeline reminderPoll = new Timeline(new KeyFrame(Duration.seconds(2),
                e -> notifications.triggerReminder()));
        reminderPoll.setCycleCount(Timeline.INDEFINITE);
        reminderPoll.play();

        TabPane tabs = new TabPane();
        Tab tasksTab = new Tab("Tasks", tasksContent);
        tasksTab.setClosable(false);
        Tab calendarTab = new Tab("Calendar", calendarBox);
        calendarTab.setClosable(false);
        tabs.getTabs().addAll(tasksTab, calendarTab);

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.getChildren().add(title);
        root.getChildren().add(new Separator());
        root.getChildren().add(tabs);

        Scene scene = new Scene(root, 760, 560);
        stage.setTitle("Assignment 6");
        stage.setScene(scene);
        stage.show();
    }

    private static int toPriorityValue(String label) {
        if (label == null) {
            throw new IllegalArgumentException("Priority must be High, Medium, or Low");
        }
        return switch (label) {
            case "High" -> 1;
            case "Medium" -> 2;
            case "Low" -> 3;
            default -> throw new IllegalArgumentException("Priority must be High, Medium, or Low");
        };
    }

    private static String toPriorityLabel(int priority) {
        return switch (priority) {
            case 1 -> "High";
            case 2 -> "Medium";
            case 3 -> "Low";
            default -> "Medium";
        };
    }

    // Calendar tab visual helpers — own the look of the rendered day cards.

    private static VBox buildDayCard(LocalDate day, List<Task> tasks, List<Event> events) {
        Label header = new Label(day + " — " + capitalize(day.getDayOfWeek().name()));
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333;");

        VBox card = new VBox(4);
        card.getChildren().add(header);

        if (tasks.isEmpty() && events.isEmpty()) {
            Label none = new Label("  Nothing scheduled.");
            none.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            card.getChildren().add(none);
        } else {
            for (Task t : tasks) card.getChildren().add(buildTaskRow(t));
            for (Event e : events) card.getChildren().add(buildEventRow(e));
        }

        card.setPadding(new Insets(8, 10, 8, 10));
        card.setStyle("-fx-background-color: #f7f7f9; -fx-background-radius: 6; -fx-border-color: #e1e1e6; -fx-border-radius: 6;");
        return card;
    }

    private static HBox buildTaskRow(Task t) {
        Circle badge = new Circle(6, priorityColor(t.getPriority()));
        badge.setStroke(Color.web("#00000022"));

        String title = (t.getTitle() == null || t.getTitle().isBlank()) ? "(Untitled Task)" : t.getTitle();
        Label titleLabel = new Label(title);
        String titleStyle = "-fx-font-size: 13px;";
        if (t.isCompleted()) {
            titleStyle += " -fx-strikethrough: true; -fx-text-fill: #888;";
        } else {
            titleStyle += " -fx-text-fill: #222;";
        }
        titleLabel.setStyle(titleStyle);

        Label meta = new Label(toPriorityLabel(t.getPriority()) + " priority  ·  due " + t.getDeadline()
                + (t.isCompleted() ? "  ·  Done" : ""));
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        VBox text = new VBox(2, titleLabel, meta);
        if (t.getDescription() != null && !t.getDescription().isBlank()) {
            Label desc = new Label(t.getDescription());
            desc.setWrapText(true);
            desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
            text.getChildren().add(desc);
        }

        HBox row = new HBox(10, badge, text);
        row.setPadding(new Insets(4, 0, 4, 6));
        HBox.setHgrow(text, Priority.ALWAYS);
        return row;
    }

    private static HBox buildEventRow(Event e) {
        Circle badge = new Circle(6, Color.web("#6c757d"));
        badge.setStroke(Color.web("#00000022"));

        String title = (e.getTitle() == null || e.getTitle().isBlank()) ? "(Untitled Event)" : e.getTitle();
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #222;");

        StringBuilder metaText = new StringBuilder();
        metaText.append(e.getStartTime()).append("–").append(e.getEndTime());
        if (e.getLocation() != null && !e.getLocation().isBlank()) {
            metaText.append("  ·  @ ").append(e.getLocation());
        }
        Label meta = new Label(metaText.toString());
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        VBox text = new VBox(2, titleLabel, meta);
        HBox row = new HBox(10, badge, text);
        row.setPadding(new Insets(4, 0, 4, 6));
        HBox.setHgrow(text, Priority.ALWAYS);
        return row;
    }

    // Priority colors: High = red, Medium = amber, Low = green.
    private static Color priorityColor(int priority) {
        return switch (priority) {
            case 1 -> Color.web("#e74c3c");
            case 2 -> Color.web("#f39c12");
            case 3 -> Color.web("#27ae60");
            default -> Color.web("#9e9e9e");
        };
    }

    // Accepts a variety of common time inputs so users don't have to remember
    // the strict HH:mm format: "5:30", "05:30", "17:30", "5:30 PM", "5 pm".
    private static LocalTime parseTime(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toUpperCase();
        if (s.isEmpty()) return null;

        boolean isPM = s.endsWith("PM");
        boolean isAM = s.endsWith("AM");
        if (isPM || isAM) {
            s = s.substring(0, s.length() - 2).trim();
        }

        if (!s.contains(":")) {
            s = s + ":00";
        }

        int colon = s.indexOf(':');
        String hourPart = s.substring(0, colon).trim();
        String minutePart = s.substring(colon + 1).trim();
        if (hourPart.length() == 1) hourPart = "0" + hourPart;
        if (minutePart.length() == 1) minutePart = "0" + minutePart;
        if (minutePart.isEmpty()) minutePart = "00";

        try {
            int hour = Integer.parseInt(hourPart);
            int minute = Integer.parseInt(minutePart);
            if (isPM && hour < 12) hour += 12;
            if (isAM && hour == 12) hour = 0;
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null;
            return LocalTime.of(hour, minute);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
