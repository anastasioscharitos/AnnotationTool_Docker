package com.example.application.backEnd.scraping;

import java.util.List;

/**
 * Η κλάση WebPage αντιπροσωπεύει μια σελίδα. Έχει ορίσματα το τίτλο της σελίδας,
 * το κείμενο, τη διεύθυνση και μια λίστα από εικόνες (αντικείμενα Images)
 */
public class WebPage {

    private String title;
    private String page;
    private String url;
    private List<Images> images;

    /**
     * Κατασκευαστής κλάσης
     */
    public WebPage() {
    }

    /**
     * Κατασκευαστής κλάσης
     * @param url, η διεύθυνση url
     * @param title, ο τίτλος της σελίδας
     * @param page, το κείμενο της σελίδας
     * @param images, λίστα με εικόνες, αντικείμενα Images
     */
    public WebPage(String url, String title, String page, List<Images> images) {
        this.title = title;
        this.page = page;
        this.url = url;
        this.images = images;
    }

    /**
     * Getters και Setters της κλάσης
     * @return
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Images> getImages() {
        return images;
    }

    public void setImages(List<Images> images) {
        this.images = images;
    }
}
