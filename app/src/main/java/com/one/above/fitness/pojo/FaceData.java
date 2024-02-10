package com.one.above.fitness.pojo;

import android.graphics.Bitmap;

import java.io.Serializable;

public class FaceData implements Serializable {
    private String id;
    private String name;
    private String memberID;
    private Float distance;
    private Object extra;
    private String startTime;
    private String endTime;
    private String timeFormat;
    private String Branchno;
    private transient Bitmap userImage;

    public FaceData(String id, String name, String memberID, Float distance, Object extra, String startTime, String endTime, String timeFormat,
                    Bitmap userImage, String Branchno) {
        this.id = id;
        this.name = name;
        this.memberID = memberID;
        this.distance = distance;
        this.extra = extra;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeFormat = timeFormat;
        this.userImage = userImage;
        this.Branchno = Branchno;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public Bitmap getUserImage() {
        return userImage;
    }

    public void setUserImage(Bitmap userImage) {
        this.userImage = userImage;
    }

    public String getBranchno() {
        return Branchno;
    }

    public void setBranchno(String branchno) {
        Branchno = branchno;
    }

    @Override
    public String toString() {
        return "FaceData{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", memberID='" + memberID + '\'' +
                ", distance=" + distance +
                ", extra=" + extra +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", timeFormat='" + timeFormat + '\'' +
                ", Branchno='" + Branchno + '\'' +
                ", userImage=" + userImage +
                '}';
    }
}
