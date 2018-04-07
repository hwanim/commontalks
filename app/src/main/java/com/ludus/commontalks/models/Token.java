package com.ludus.commontalks.models;

import lombok.Data;

/**
 * Created by imhwan on 2018. 1. 24..
 */
@Data
public class Token {

    private String uid, chatId, chatTxt, oppositeUserNickname, oppositeUserUrl, oppositeUserUid;
    private ChatType chatType;

    public enum  ChatType {
        NEW_CHAT, MSG, COMPLIMENT
    }

}
