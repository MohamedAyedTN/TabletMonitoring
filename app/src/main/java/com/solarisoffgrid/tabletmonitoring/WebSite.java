package com.solarisoffgrid.tabletmonitoring;

public class WebSite {
    private String website_title;
    private String website_url;
    private String last_visit;
    private int visits;
    private byte[] icon;

    public WebSite() {
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "WebSite{" +
                "website_title='" + website_title + '\'' +
                ", website_url='" + website_url + '\'' +
                ", last_visit='" + last_visit + '\'' +
                ", visits=" + visits + "\n" +
                '}';
    }

    public String getWebsite_title() {
        return website_title;
    }

    public void setWebsite_title(String website_title) {
        this.website_title = website_title;
    }

    public String getWebsite_url() {
        return website_url;
    }

    public void setWebsite_url(String website_url) {
        this.website_url = website_url;
    }

    public String getLast_visit() {
        return last_visit;
    }

    public void setLast_visit(String last_visit) {
        this.last_visit = last_visit;
    }

    public int getVisits() {
        return visits;
    }

    public void setVisits(int visits) {
        this.visits = visits;
    }
}
