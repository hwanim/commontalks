package com.ludus.commontalks.Services;

import com.ludus.commontalks.models.User;

import lombok.Getter;

/**
 * Created by imhwan on 2017. 12. 20..
 */

@Getter
public class UserDataChangeEvent {
    private User mUser;

    public UserDataChangeEvent(User user ) {
        this.mUser = user;
    }

}
