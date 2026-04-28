package com.example.application.backEnd.scraping;

import java.util.Objects;

/**
 * Η κλάση Images αντιπροσωπεύει μια εικόνα. Έχει ορίσματα τη διεύθυνση της σελίδας,
 * τη διεύθυνση της εικόνας, τη λεζάντας και κατάσταση επιλογής
 */
public class Images {

    private String url;
    private String urlImage;
    private String caption;
    private boolean isSelected;

    /**
     * Κατασκευαστής κλάσης
     */
    public Images() {
    }

    /**
     * Κατασκευαστής κλάσης
     * @param url
     * @param urlImage
     * @param caption
     */
    public Images(String url, String urlImage, String caption) {
        this.url = url;
        this.urlImage = urlImage;
        this.caption = caption;
        this.isSelected = false;
    }

    /**
     * Getters και setters κλάσης
     * @return
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     * Οι μέθοδοι equals και hashCode χρησιμοποιούνται για τη σύγκριση δύο αντικειμένων Images.
     * Αυτό γίνεται για να μην εμφανιστούν διπλότυπες εικόνες (Images)
     * Η σύγκριση γίνεται με βάση τη διεύθυνση κάθε εικόνας
     * @param obj   the reference object with which to compare.
     * @return
     */
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }

        Images that = (Images) obj;

        Boolean bool = Objects.equals(urlImage, that.urlImage);

        return bool;
    }

    public int hashCode() {
        return Objects.hash(urlImage);
    }
}
