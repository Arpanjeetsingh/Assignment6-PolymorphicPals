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
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import manager.NotificationManager;
import model.Calendar;
import model.Reminder;
import model.Task;
import model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        reminderTimeField.setPromptText("Time (HH:mm)");

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
            } else {
                reminderMessageField.clear();
                reminderDatePicker.setValue(null);
                reminderTimeField.clear();
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

        VBox right = new VBox(10, selectedTaskLabel, form, taskButtons, otherButtons);
        right.setPadding(new Insets(12));

        VBox left = new VBox(10, new Label("Tasks"), taskList);
        left.setPadding(new Insets(12));

        HBox tasksContent = new HBox(12, left, right);
        HBox.setHgrow(right, Priority.ALWAYS);

        // ===== Calendar tab =====
        DatePicker calendarDatePicker = new DatePicker(LocalDate.now());
        Button dailyBtn = new Button("Daily");
        Button weeklyBtn = new Button("Weekly");
        Button monthlyBtn = new Button("Monthly");
        Button refreshBtn = new Button("Refresh");
        Label calendarStatus = new Label("View: Daily");

        TextArea calendarOutput = new TextArea();
        calendarOutput.setEditable(false);
        calendarOutput.setPrefRowCount(20);

        Runnable renderCalendar = () -> {
            LocalDate picked = calendarDatePicker.getValue();
            if (picked != null) {
                calendar.setSelectedDate(picked);
            }
            calendarOutput.setText(calendar.render());
            calendarStatus.setText("View: " + capitalize(calendar.getCurrentView().name()));
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
        refreshBtn.setOnAction(e -> renderCalendar.run());
        calendarDatePicker.valueProperty().addListener((obs, o, n) -> renderCalendar.run());

        // Re-render the calendar whenever the task list changes so newly
        // saved/deleted tasks show up without the user pressing Refresh.
        tasks.addListener((javafx.collections.ListChangeListener<Task>) c -> renderCalendar.run());

        HBox calendarControls = new HBox(8, new Label("Date:"), calendarDatePicker,
                dailyBtn, weeklyBtn, monthlyBtn, refreshBtn);
        VBox calendarBox = new VBox(10, calendarControls, calendarStatus, calendarOutput);
        calendarBox.setPadding(new Insets(12));

        renderCalendar.run();

        // Wire NotificationManager to surface popups on the JavaFX thread.
        notifications.setListener(r -> Platform.runLater(() ->
                showInfo("Reminder", r.getMessage() == null ? "(no message)" : r.getMessage())));

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

    private static LocalTime parseTime(String hhmm) {
        if (hhmm == null) return null;
        String trimmed = hhmm.trim();
        if (trimmed.isEmpty()) return null;
        try {
            return LocalTime.parse(trimmed);
        } catch (Exception ignored) {
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
