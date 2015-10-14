package ru.zeyuzh.testrssreader;

import java.util.ArrayList;
import java.util.List;

public class RSSFeed {
    private String title = "";
    private String description = "";
    private String link = "";

    private List<RSSMessage> entries = new ArrayList<RSSMessage>();

    public void addEntries(RSSMessage message) {
        entries.add(message);
    }

    public List<RSSMessage> getEntries() {
        return entries;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "Feed [title= " + title + " description= " + description
                + ", link= " + link + "]";
    }
}
