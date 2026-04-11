package com.example.tour_management.dto.email;

public class ResetPasswordEmailMessage {

    private String email;
    private String link;

    public ResetPasswordEmailMessage() {}

    public ResetPasswordEmailMessage(String email, String link) {
        this.email = email;
        this.link = link;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}