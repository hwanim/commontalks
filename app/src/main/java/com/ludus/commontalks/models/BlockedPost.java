package com.ludus.commontalks.models;

import lombok.Data;

/**
 * Created by imhwan on 2017. 12. 13..
 */
@Data
public class BlockedPost {


    public BlockedPost() {

    }

    public BlockedPost(String postUserId, String postId, blockTypeEnum blockType) {
        this.postUserId = postUserId;
        this.postId = postId;
        this.blockType = blockType;
    }

    private String postUserId, postId;
    private blockTypeEnum blockType;


    public enum blockTypeEnum {
        alreadyRead, reported
    }
}
