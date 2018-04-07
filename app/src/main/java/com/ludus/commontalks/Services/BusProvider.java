package com.ludus.commontalks.Services;

import com.squareup.otto.Bus;

/**
 * Created by imhwan on 2017. 12. 5..
 */

public class BusProvider {

    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }

}
