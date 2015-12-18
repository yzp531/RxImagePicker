package com.yokeyword.rximagepicker.model.event;

/**
 * 切换DetailFragment事件
 * Created by YoKeyword on 15/12/16.
 */
public class AddDetailEvent {
    private String bucketName;

    public AddDetailEvent(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
