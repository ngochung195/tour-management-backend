package com.example.tour_management.dto.dashboard;

public class RecentActivityResponse {

    private String type;
    private String title;
    private String subtitle;
    private String timeAgo;

    public RecentActivityResponse(String type, String title, String subtitle, String timeAgo) {
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.timeAgo = timeAgo;
    }

    public String getType()     { return type; }
    public String getTitle()    { return title; }
    public String getSubtitle() { return subtitle; }
    public String getTimeAgo()  { return timeAgo; }
}