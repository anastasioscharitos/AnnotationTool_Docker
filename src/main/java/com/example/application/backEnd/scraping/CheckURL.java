package com.example.application.backEnd.scraping;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Η μέθοδος CheckURL ελέγχει το url
 */
public class CheckURL {

    private String url;

    /**
     * Κατασκευαστής κλάσης
     */
    public CheckURL() {
    }

    /**
     * Κατασκευαστής κλάσης με όρισμα τη διεύθυνση url
     * @param url η διεύθυνση url
     */
    public CheckURL(String url) {
        this.url = url;
    }

    /**
     * Έλεγχος εγκυρότητας του ulr, ο έλεγχος γίνεται με την προσπάθεια δημιουργίας
     * ενός αντικειμένου URI και τη μετατροπή του σε URL. Αν η σύνταξη είναι λάθος
     * επιστρέφει false
     * @return true αν δεν υπάρχει συντακτικό λάθος διαφορετικά false
     */
    public boolean isValid(){
        try{
            URI.create(url).toURL();
            return true;
        } catch (MalformedURLException | IllegalArgumentException e) {
            return false;
        }
    }
}
