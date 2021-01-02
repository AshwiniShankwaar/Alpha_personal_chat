package com.nanb.alpha.Notification;

public class Data {
    private String user,body,tittle,sented;
    private int icon;

    public Data(String user, String body, String tittle, String sented, int icon) {
        this.user = user;
        this.body = body;
        this.tittle = tittle;
        this.sented = sented;
        this.icon = icon;
    }
    public Data(){
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public String getSented() {
        return sented;
    }

    public void setSented(String sented) {
        this.sented = sented;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
