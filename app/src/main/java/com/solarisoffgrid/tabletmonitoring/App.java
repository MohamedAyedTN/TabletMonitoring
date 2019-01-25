package com.solarisoffgrid.tabletmonitoring;


import android.graphics.drawable.Drawable;

public class App {
    private String App_name;
    private String last_use;
    private long used_for;
    private String category;
    private long data_sent;
    private long data_received;
    private Drawable icon;


    public App() {
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "App{" +
                "App_name='" + App_name + '\'' +
                ", last_use='" + last_use + '\'' +
                ", used_for='" + used_for + '\'' +
                ", category='" + category + '\'' +
                ", data_sent='" + data_sent + '\'' +
                ", data_received='" + data_received + '\'' + "\n" +
                '}';
    }

    public String getApp_name() {
        return App_name;
    }

    public void setApp_name(String app_name) {
        App_name = app_name;
    }

    public String getLast_use() {
        return last_use;
    }

    public void setLast_use(String last_use) {
        this.last_use = last_use;
    }

    public long getUsed_for() {
        return used_for;
    }

    public void setUsed_for(long used_for) {
        this.used_for = used_for;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getData_sent() {
        return data_sent;
    }

    public void setData_sent(long data_sent) {
        this.data_sent = data_sent;
    }

    public long getData_received() {
        return data_received;
    }

    public void setData_received(long data_received) {
        this.data_received = data_received;
    }
}
