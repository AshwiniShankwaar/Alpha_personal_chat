package com.nanb.alpha;

public class commentmodelclass {
    private String publisherid,postids;

    public commentmodelclass() {
    }

    public commentmodelclass(String publisherid, String postids) {
        this.publisherid = publisherid;
        this.postids = postids;
    }

    public String getPublisherid() {
        return publisherid;
    }

    public void setPublisherid(String publisherid) {
        this.publisherid = publisherid;
    }

    public String getPostids() {
        return postids;
    }

    public void setPostids(String postids) {
        this.postids = postids;
    }
}
