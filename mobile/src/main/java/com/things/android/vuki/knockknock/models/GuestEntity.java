package com.things.android.vuki.knockknock.models;

/**
 * Created by mvukosav
 */
public class GuestEntity {

    Long timestamp;
    String image;
    boolean isAccepted;

    public GuestEntity() {
    }

    public GuestEntity( Long timestamp, String image ) {
        this.timestamp = timestamp;
        this.image = image;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getImage() {
        return image;
    }

    public void setAccepted( boolean accepted ) {
        isAccepted = accepted;
    }

    public boolean isAccepted() {
        return isAccepted;
    }
}
