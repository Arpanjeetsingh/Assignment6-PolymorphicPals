package model;

public class Event {

    private int eventID;
    private String title;
    private String date;
    private String startTime;
    private String endTime;
    private String location;

    public Event(int eventID, String title, String date,
                 String startTime, String endTime, String location) {

        this.eventID = eventID;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void scheduleEvent() {
        System.out.println("Event scheduled: " + title);
    }

    public void updateEvent(String newTitle, String newDate,
                            String newStartTime, String newEndTime,
                            String newLocation) {

        title = newTitle;
        date = newDate;
        startTime = newStartTime;
        endTime = newEndTime;
        location = newLocation;

        System.out.println("Event updated successfully.");
    }

    public void cancelEvent() {
        System.out.println("Event cancelled: " + title);
    }

    @Override
    public String toString() {
        return "Event ID: " + eventID +
                "\nTitle: " + title +
                "\nDate: " + date +
                "\nStart Time: " + startTime +
                "\nEnd Time: " + endTime +
                "\nLocation: " + location;
    }
}