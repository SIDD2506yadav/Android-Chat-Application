package com.example.mfast;

public class SearchUsers {
    private String Name,ProfilePicture,Friends,Profession,Status,message;

    public SearchUsers(){

    }

    public SearchUsers(String name, String profilePicture, String friends, String profession, String status, String message) {
        Name = name;
        ProfilePicture = profilePicture;
        Friends = friends;
        Profession = profession;
        Status = status;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getProfession() {
        return Profession;
    }

    public void setProfession(String profession) {
        Profession = profession;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getProfilePicture() {
        return ProfilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        ProfilePicture = profilePicture;
    }

    public String getFriends() {
        return Friends;
    }

    public void setFriends(String friends) {
        Friends = friends;
    }
}
