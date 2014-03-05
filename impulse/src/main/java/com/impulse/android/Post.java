package com.impulse.android;

/**
 * Created by Eliot on 3/2/14.
 */
public class Post {

    public double lon;
    public double lat;
    public String caption;
    public String fileName;
    public String timeOut;

    public Post(double lon, double lat, String caption, String fileName, String timeOut) {
        this.lat = lat;
        this.lon = lon;
        this.caption = caption;
        this.fileName = fileName;
        this.timeOut = timeOut;
    }
}