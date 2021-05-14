package app.olivs.OnGate.Classes;

public class CheckInRecord {
    private int id;
    private int userID;
    private int eventID;
    private String timestamp, photoURL;
    private boolean synced;
    private ActivityState type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public ActivityState getType() {
        return type;
    }

    public void setType(ActivityState type) {
        this.type = type;
    }

    public CheckInRecord(int userID, int eventID, String timestamp, String photoURL, boolean synced, ActivityState type) {
        this.userID = userID;
        this.eventID = eventID;
        this.timestamp = timestamp;
        this.photoURL = photoURL;
        this.synced = synced;
        this.type = type;
    }

    public CheckInRecord(int id, int userID, int eventID, String timestamp, String photoURL, boolean synced, ActivityState type) {
        this.id = id;
        this.userID = userID;
        this.eventID = eventID;
        this.timestamp = timestamp;
        this.photoURL = photoURL;
        this.synced = synced;
        this.type = type;
    }
}
