package com.nanb.alpha;

public class message {
    private String ffrom, message, type,to,date,time,messageId,name,tempfile;

    public message(){

    }

    public message(String ffrom, String message, String type, String to, String date, String time, String messageId,String name,String tempfile) {
        this.ffrom = ffrom;
        this.message = message;
        this.type = type;
        this.to = to;
        this.date = date;
        this.time = time;
        this.messageId = messageId;
        this.name = name;
        this.tempfile = tempfile;
    }

    public String getFfrom() {
        return ffrom;
    }

    public void setFfrom(String ffrom) {
        this.ffrom = ffrom;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }
    public String gettemp() {
        return tempfile;
    }

    public void settemp(String tempfile) {
        this.tempfile = tempfile;
    }
}
