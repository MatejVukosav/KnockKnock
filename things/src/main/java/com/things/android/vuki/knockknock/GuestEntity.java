package com.things.android.vuki.knockknock;

/**
 * Created by mvukosav
 */
public class GuestEntity {

    public Long timestamp;
    public String image;
    public Boolean isAccepted;

    public GuestEntity( Long timestamp, String image ) {
        this.timestamp = timestamp;
        this.image = image;
        this.isAccepted = null;
    }

    public GuestEntity() {
    }
}
