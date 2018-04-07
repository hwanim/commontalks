package com.ludus.commontalks.models;

import lombok.Data;

/**
 * Created by imhwan on 2018. 1. 5..
 */
@Data
public class TutorialCheck {
    private String uid;
    private boolean swipeTuto = false,
            leftSwipeTuto = false,
            rightSwipeTuto = false,
            chatTuto = false,
            ratingTuto = false,
            timerTuto = false,
            chatLimitTuto = false;
}
