package com.reachskyline.reachher;

public class DriveLink {

    private String desc, links;

    public DriveLink() {}

    public DriveLink(String desc, String links) {
        this.desc = desc;
        this.links = links;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(String links) {
        this.links = links;
    }
}
