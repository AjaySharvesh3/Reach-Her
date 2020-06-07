package com.reachskyline.reachher;

public class DriveLink {

    private String desc, link;

    public DriveLink() {}

    public DriveLink(String desc, String link) {
        this.desc = desc;
        this.link = link;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
