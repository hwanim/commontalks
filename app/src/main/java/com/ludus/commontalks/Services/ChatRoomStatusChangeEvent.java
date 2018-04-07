package com.ludus.commontalks.Services;

import com.ludus.commontalks.models.ChatRoom;

import lombok.Getter;

/**
 * Created by imhwan on 2018. 1. 5..
 */

@Getter
public class ChatRoomStatusChangeEvent {
    private ChatRoom.chatRoomStatusEnum chatRoomStatusEnum;

    public ChatRoomStatusChangeEvent(ChatRoom.chatRoomStatusEnum chatRoomStatusEnum) {
        this.chatRoomStatusEnum = chatRoomStatusEnum;
    }
}
