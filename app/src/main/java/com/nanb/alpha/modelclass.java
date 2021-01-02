package com.nanb.alpha;

public class modelclass {
    private String GroupprofileName,StatusGroup,profileimage;

    public modelclass() {
    }

    public modelclass(String groupprofileName, String statusGroup, String profileimage) {
        this.GroupprofileName = groupprofileName;
        this.StatusGroup = statusGroup;
        this.profileimage = profileimage;
    }

    public String getGroupprofileName() {
        return GroupprofileName;
    }

    public void setGroupprofileName(String groupprofileName) {
        GroupprofileName = groupprofileName;
    }

    public String getStatusGroup() {
        return StatusGroup;
    }

    public void setStatusGroup(String statusGroup) {
        StatusGroup = statusGroup;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}
