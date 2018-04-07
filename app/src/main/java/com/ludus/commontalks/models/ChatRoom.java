package com.ludus.commontalks.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

import lombok.Data;

/**
 * Created by imhwan on 2017. 11. 23..
 */

@Data
public class ChatRoom {

    private String chatId, lastMessage, lastMessageUserId;
    private long createDate, lastMessageDate, disappearTime;
    private User chatOppositeUser;
    private Boolean disabled = false;
    private Boolean itemUsed = false;
    private Boolean readMsg = true;
    private Boolean continuouslySend = false;
    private chatRoomStatusEnum chatRoomStatus = chatRoomStatusEnum.ACTIVE;


    public enum chatRoomStatusEnum {
        ACTIVE, NOTRATED, RATING, DISABLED
    }

}
