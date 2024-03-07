package com.one.above.fitness.pojo;

public class FirebaseUserData {
    private String id;
    private String name;
    private String memberID;
    private String distance;
    private String extra;
    private String startTime;
    private String endTime;
    private String timeFormat;
    private String userImage;
    private String Branchno;

    private String type;
    public FirebaseUserData() {
    }
    public FirebaseUserData(String id, String name, String memberID, String distance, String extra, String startTime, String endTime, String timeFormat,
                            String userImage, String Branchno, String type) {
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
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
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

    public String getBranchno() {
        return Branchno;
    }

    public void setBranchno(String branchno) {
        Branchno = branchno;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }
}
