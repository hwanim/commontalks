package com.ludus.commontalks.models;

import com.ludus.commontalks.Services.NotificationService;

import java.util.ArrayList;

import lombok.Data;

/**
 * Created by imhwan on 2017. 12. 21..
 */
@Data
public class NotificationSetting {


    private String Uid;
    private ArrayList<Boolean> notiSetting;

    public NotificationSetting(String uid, ArrayList<Boolean> notisetting) {
        this.Uid = uid;
        this.notiSetting = notisetting;
    }

}
