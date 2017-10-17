package com.things.android.vuki.knockknock.models;

/**
 * Created by mvukosav
 */
public class GuestEntity {

    public Long timestamp;
    public String image;
    //Can't parse if private
    public Boolean isAccepted;

    @SuppressWarnings("unused")
    public GuestEntity( Long timestamp, String image, Boolean isAccepted ) {
        this.timestamp = timestamp;
        this.image = image;
        this.isAccepted = isAccepted;
    }

    public GuestEntity() {
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp( Long timestamp ) {
        this.timestamp = timestamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage( String image ) {
        this.image = image;
    }

    public Boolean getAccepted() {
        return isAccepted;
    }

    public void setAccepted( Boolean accepted ) {
        isAccepted = accepted;
    }
}
