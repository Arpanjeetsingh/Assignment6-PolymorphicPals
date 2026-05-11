package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class User {
    private final String userId;
    private String username;
    private String email;
    private String password;
    private boolean loggedIn;

    private final List<Task> taskList;
    private final List<Event> eventList;

    //Creates a new user with a randomly generated user ID.
    public User(String username, String email, String password) {
        this.userId = UUID.randomUUID().toString();
        setUsername(username);
        setEmail(email);
        setPassword(password);

        this.loggedIn = false;
        this.taskList = new ArrayList<>();
        this.eventList = new ArrayList<>();
    }

    //Attempts to log the user in.
    //The user can log in using either their username or email.
    public boolean login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) {
            return false;
        }

        boolean matchesUsername = this.username.equalsIgnoreCase(usernameOrEmail.trim());
        boolean matchesEmail = this.email.equalsIgnoreCase(usernameOrEmail.trim());
        boolean matchesPassword = this.password.equals(password);

        if ((matchesUsername || matchesEmail) && matchesPassword) {
            loggedIn = true;
            return true;
        }

        return false;
    }

    
    public void logout() {
        loggedIn = false;
    }

    
    public void createTask(Task task) {
        requireLogin();

        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }

        taskList.add(task);
    }

   
    public boolean deleteTask(Task task) {
        requireLogin();

        if (task == null) {
            return false;
        }

        return taskList.remove(task);
    }

    
    public void createEvent(Event event) {
        requireLogin();

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        eventList.add(event);
    }

    
    public boolean deleteEvent(Event event) {
        requireLogin();

        if (event == null) {
            return false;
        }

        return eventList.remove(event);
    }

    
    private void requireLogin() {
        if (!loggedIn) {
            throw new IllegalStateException("User must be logged in to perform this action.");
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be blank.");
        }

        this.username = username.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be blank.");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email must contain @.");
        }

        this.email = email.trim();
    }

    /**
     * There is intentionally no public getPassword() method.
     * Other classes should not directly access the user's password.
     */
    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be blank.");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        this.password = password;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public List<Task> getTaskList() {
        return Collections.unmodifiableList(taskList);
    }

    public List<Event> getEventList() {
        return Collections.unmodifiableList(eventList);
    }
}