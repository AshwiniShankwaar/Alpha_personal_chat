package com.nanb.alpha;

public class postmodelclass {
    private String PostAdmin,type;

    public postmodelclass() {
    }

    public postmodelclass(String postAdmin, String type) {
        this.PostAdmin = postAdmin;
        this.type = type;
    }

    public String getPostAdmin() {
        return PostAdmin;
    }

    public void setPostAdmin(String postAdmin) {
        PostAdmin = postAdmin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
