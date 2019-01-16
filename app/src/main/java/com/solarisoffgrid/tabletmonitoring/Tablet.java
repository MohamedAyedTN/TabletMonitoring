package com.solarisoffgrid.tabletmonitoring;

import java.util.List;

public class Tablet {

    private String tablet_serial;
    private String client_phone;
    private String report_date;
    private Boolean payg_status;
    private String expirtaion_date;
    private String tablet_password;
    private List<App> top_app;
    private List<WebSite> top_website;

    public Tablet() {
    }

    public String getReport_date() {
        return report_date;
    }

    public void setReport_date(String report_date) {
        this.report_date = report_date;
    }

    public String getTablet_serial() {
        return tablet_serial;
    }

    public void setTablet_serial(String tablet_serial) {
        this.tablet_serial = tablet_serial;
    }

    public String getClient_phone() {
        return client_phone;
    }

    public void setClient_phone(String client_phone) {
        this.client_phone = client_phone;
    }

    public Boolean getPayg_status() {
        return payg_status;
    }

    public void setPayg_status(Boolean payg_status) {
        this.payg_status = payg_status;
    }

    public String getExpirtaion_date() {
        return expirtaion_date;
    }

    public void setExpirtaion_date(String expirtaion_date) {
        this.expirtaion_date = expirtaion_date;
    }

    public String getTablet_password() {
        return tablet_password;
    }

    public void setTablet_password(String tablet_password) {
        this.tablet_password = tablet_password;
    }

    public List<App> getTop_app() {
        return top_app;
    }

    public void setTop_app(List<App> top_app) {
        this.top_app = top_app;
    }

    public List<WebSite> getTop_website() {
        return top_website;
    }

    public void setTop_website(List<WebSite> top_website) {
        this.top_website = top_website;
    }


}
