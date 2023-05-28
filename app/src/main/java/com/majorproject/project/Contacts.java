package com.majorproject.project;

public class Contacts {
    public String getName() {
        return name;
    }

    public Contacts() {
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

    public String name;
    public String status;

    public Contacts(String name, String status) {
        this.name = name;
        this.status = status;
    }



}
