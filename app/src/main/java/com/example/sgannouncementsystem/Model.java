package com.example.sgannouncementsystem;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Model {
    String id;
    String title;
    String details;
    String admin;
    @ServerTimestamp Date time;

    public Model() {
    }

    public Model(String id, String title, String details, String admin, Date time) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.admin = admin;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
