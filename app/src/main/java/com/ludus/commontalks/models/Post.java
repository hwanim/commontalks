package com.ludus.commontalks.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Date;

import lombok.Data;

/**
 * Created by imhwan on 2017. 11. 21..
 */

@Data
public class Post {

    private String postId, postTxt, photoUrl;
    private User user;
    private long postDate;
    private int chatLimitValue, reportCount;
    private postType whichPost;
    private ArrayList<String> hashTags;

    public enum postType {
        TXT, PHOTO
    }
}
