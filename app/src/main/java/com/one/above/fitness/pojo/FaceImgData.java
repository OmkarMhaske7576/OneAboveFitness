package com.one.above.fitness.pojo;

import android.graphics.Bitmap;

public class FaceImgData {
    private String id;
    private String userId;
    private Bitmap imageData;
    private Object extra;
    private int isSelected = 0;

    public FaceImgData(String id, String userId, Bitmap imageData, Object extra, int isSelected) {
        this.id = id;
        this.userId = userId;
        this.imageData = imageData;
        this.extra = extra;
        this.isSelected = isSelected;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Bitmap getImageData() {
        return imageData;
    }

    public void setImageData(Bitmap imageData) {
        this.imageData = imageData;
    }

    public int getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(int isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public String toString() {
        return "FaceImgData{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", imageData=" + imageData +
                ", extra=" + extra +
                ", isSelected=" + isSelected +
                '}';
    }
}
