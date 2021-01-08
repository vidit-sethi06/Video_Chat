package com.example.videochat;

public class Contacts {
    String name,bio,uid,image;

    public Contacts() {
    }

    public Contacts(String name, String bio, String uid, String image) {
        this.name = name;
        this.bio = bio;
        this.uid = uid;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
