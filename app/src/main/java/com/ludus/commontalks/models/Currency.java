package com.ludus.commontalks.models;

import java.util.ArrayList;

import lombok.Data;

/**
 * Created by imhwan on 2017. 12. 11..
 */
@Data
public class Currency {

    private long coinCount, jamCount;
    private String uid;
    private ArrayList<String> depositLog, withdrawLog;

}
