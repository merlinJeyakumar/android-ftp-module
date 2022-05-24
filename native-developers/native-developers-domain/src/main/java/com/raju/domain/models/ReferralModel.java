package com.raju.domain.models;

public class ReferralModel {
    public String utm_source; //comes from, eg. referred by,name,id, email, app, link
    public String utm_medium; //CTC source, clicked source name eg. app, email or else
    public String utm_term; //referred item
    public String utm_content; //referred item attachment or whatever
    public String utm_campaign; //campaign name
}