package com.ludus.commontalks.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

import lombok.Data;

/**
 * Created by imhwan on 2017. 11. 23..
 */

@Data
public class Message {
    private String messageId, messageText;
    private User messageUser;
    private long messageDate;
    private boolean readMessage, continuoslySend, initMessage = false;
    private messageTypeEnum messageType;

    public enum messageTypeEnum{
        TXT, PHOTO, INFOMATION, END, INIT
    }

}
