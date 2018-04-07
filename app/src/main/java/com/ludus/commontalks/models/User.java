package com.ludus.commontalks.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import lombok.Data;

/**
 * Created by imhwan on 2017. 11. 19..
 */
@Data
public class User {

    private String uid, email, username, profileUrl, nickname, birth;
    private long lastLogin;
    private int sex;
    private HashMap<String, Integer> ratings;


    public User() {
        this.ratings = new HashMap<>();
        this.ratings.put("gentle", 0);
        this.ratings.put("kind", 0);
        this.ratings.put("humorous", 0);
        this.ratings.put("caring", 0);
        this.ratings.put("popular", 0);
    }

    public User(String uid, String email, String username, String profileUrl, String nickname) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.profileUrl = profileUrl;
        this.nickname = nickname;

        this.ratings = new HashMap<>();
        this.ratings.put("gentle", 0);
        this.ratings.put("kind", 0);
        this.ratings.put("humorous", 0);
        this.ratings.put("caring", 0);
        this.ratings.put("popular", 0);
    }


}
