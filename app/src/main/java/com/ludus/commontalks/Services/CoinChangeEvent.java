package com.ludus.commontalks.Services;

import lombok.Getter;

/**
 * Created by imhwan on 2017. 12. 20..
 */
@Getter
public class CoinChangeEvent {

    private long CoinCount;

    public CoinChangeEvent(long CoinCount) {
        this.CoinCount = CoinCount;
    }

}
