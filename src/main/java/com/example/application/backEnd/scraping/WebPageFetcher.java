package com.example.application.backEnd.scraping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Η κλάση WebPageFetcher αναλαμβάνει την ανάκτηση του περιεχομένου μιας σελίδας.
 * Χρησιμοποιεί τη βιβλιοθήκηJsoup για τη δημιουργία σύνδεσης και αποστολής αιτημάτων
 * και επιστρέφει το έγγραφο σε μορφή Document
 */
public class WebPageFetcher {

    /**
     * Πραγματοποιεί σύνδεση σε συγκεκριμένο url και ανακτά το HTML.
     * Περιλαμβάνει ρύθμιση User-Agent για να προσομοιώνει πραγματικό χρήστη και
     * timeout για μεγάλη αναμονή.
     * @param url η διεύθυνση προς ανάκτηση
     * @return αντικείμενο Document με το HTML της σελίδας
     * @throws IOException μη έγκυρη διεύθυνση ή σφάλμα δικτύου
     */
    public Document fetch(String url) throws IOException {
        return Jsoup.connect(url).userAgent("Mozilla/5.0 ")
                .timeout(30000).ignoreContentType(true).get();
    }
}
