package com.stuff.blake.blocktalk.Models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Blake on 7/26/2016.
 */
public class Message {
    private String user;
    private String date;
    public static ArrayList<String> likes;
    private String content;
    private Date now = new Date();
    private SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy hh:mm aaa");

    public Message(String user, String content){
        this.user = user;
        this.content = content;
        this.date = df.format(now);
        this.likes = new ArrayList<String>();
    }

    public Message(){}

    public String getUser(){
        return this.user;
    }

    public String getDate(){
        return this.date;
    }

    public String getContent(){
        return this.content;
    }

    public ArrayList<String> getLikes(){
        return this.likes;
    }

    public static void likeIt(String user){
        if (!likes.contains(user)){
            likes.add(user);
        } else {
            likes.remove(user);
        }
    }
}