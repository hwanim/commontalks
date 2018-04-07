package com.ludus.commontalks.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

import lombok.Data;

/**
 * Created by imhwan on 2017. 11. 22..
 */
@Data
public class Notice {
    private String title, contents;
    private long date;

}
