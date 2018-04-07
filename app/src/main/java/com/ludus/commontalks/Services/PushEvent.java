package com.ludus.commontalks.Services;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by imhwan on 2017. 12. 5..
 */
@Getter
@Setter
public class PushEvent {

    private boolean getNewPosts = false;
    private boolean postStackEnd = false;
    private boolean loadWhenNoPostExists = false;

    public PushEvent(Boolean getNewPosts, Boolean postStackEnd)
    {
        this.getNewPosts = getNewPosts;
        this.postStackEnd = postStackEnd;
    }
}
