package com.example.mfast;

public class Messages {
    String message,type,from,to;
    String isSeen;

    public Messages(){

    }


    public Messages(String message, String type, String from, String to, String isSeen) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.to = to;
        this.isSeen = isSeen;
    }

    public String getIsSeen() {
        return isSeen;
    }

    public void setIsSeen(String isSeen) {
        this.isSeen = isSeen;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
