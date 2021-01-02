package com.nanb.alpha;

public class tagfriendmodelclass {
    public String name,status,image;
    public boolean isSelected;

    public tagfriendmodelclass() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public tagfriendmodelclass(String name, String status, String image, boolean isSelected) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.isSelected = isSelected;
    }
}
