package com.ludus.commontalks.Services;

import lombok.Getter;

/**
 * Created by imhwan on 2017. 12. 21..
 */
@Getter
public class NotiSettingPushEvent {

    private Boolean entireSetting;
    private Boolean set1;
    private Boolean set2;
    private Boolean set3;


    public NotiSettingPushEvent(Boolean entireSetting, Boolean set1, Boolean set2, Boolean set3){
        this.entireSetting = entireSetting;
        this.set1 = set1;
        this.set2 = set2;
        this.set3 = set3;
    }
}
