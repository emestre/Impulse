package com.impulse;

/**
 * Created by Eliot on 3/2/14.
 */
public class Post {

    public double lon;
    public double lat;
    public String caption;
    public String fileName;

    public Post(double lon, double lat, String caption, String fileName) {
        this.lat = lat;
        this.lon = lon;
        this.caption = caption;
        this.fileName = fileName;
    }
}
