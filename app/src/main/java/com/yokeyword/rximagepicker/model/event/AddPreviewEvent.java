package com.yokeyword.rximagepicker.model.event;

import java.util.ArrayList;

/**
 * 切换PreviewFragment事件
 * Created by YoKeyword on 15/12/16.
 */
public class AddPreviewEvent {
    private ArrayList<String> imgs;

    public AddPreviewEvent(ArrayList<String> imgs) {
        this.imgs = imgs;
    }

    public ArrayList<String> getImgs() {
        return imgs;
    }
}
