package com.things.android.vuki.knockknock;

/**
 * Created by mvukosav
 */
public class GuestEntity {

    Long timestamp;
    String image;
    boolean isAccepted;

    public GuestEntity( Long timestamp, String image ) {
        this.timestamp = timestamp;
        this.image = image;
        this.isAccepted = false;
    }
}
