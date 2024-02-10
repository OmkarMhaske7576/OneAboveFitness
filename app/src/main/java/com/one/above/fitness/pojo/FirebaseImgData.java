package com.one.above.fitness.pojo;

public class FirebaseImgData {
    private String id;
    private String userId;
    private String imageData;
    private String extra;
    private String isSelected;

    public FirebaseImgData() {
    }

    public FirebaseImgData(String id, String userId, String imageData, String extra, String isSelected) {
        this.id = id;
        this.userId = userId;
        this.imageData = imageData;
        this.extra = extra;
        this.isSelected = isSelected;
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

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(String isSelected) {
        this.isSelected = isSelected;
    }
}
