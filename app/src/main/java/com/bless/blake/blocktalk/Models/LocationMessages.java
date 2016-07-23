package com.bless.blake.blocktalk.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guest on 7/13/16.
 */
public class LocationMessages {
    public List<String> messages = new ArrayList<>();
    public LatLng latLng;

    public LocationMessages(LatLng latLng, List<String> messages) {
        this.messages = messages;
        this.latLng = latLng;
    }

    public LocationMessages(){};

    public LatLng getLatLng(){
        return this.latLng;
    }

    public List<String> getMessages(){
        return this.messages;
    }

}