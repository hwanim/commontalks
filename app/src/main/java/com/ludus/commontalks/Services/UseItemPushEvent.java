package com.ludus.commontalks.Services;

import android.util.Log;

import lombok.Getter;

/**
 * Created by imhwan on 2017. 12. 17..
 */
@Getter
public class UseItemPushEvent {

    private String mChatId;
    private itemTypeEnum mItemType;
    private long mItemUsingTime, mCoincount;

    public UseItemPushEvent(String chatId, itemTypeEnum itemType, long itmeUsingTime, long coincount) {
        Log.i("PushEvent", "push event initialized");
        this.mChatId = chatId;
        this.mItemType = itemType;
        this.mItemUsingTime = itmeUsingTime;
        this.mCoincount = coincount;
    }


    public enum itemTypeEnum {
        ONEHOUR, TWOHOUR, FOURHOUR, NIGHTSHIFT
    }

}
