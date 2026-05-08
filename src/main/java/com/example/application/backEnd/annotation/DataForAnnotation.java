package com.example.application.backEnd.annotation;

import java.util.List;

/**
 * Σε αυτήν τηn κλάση δημιουργούνται τα αντικείμενα που στέλνονται στην python για
 * επισήμανση. H λίστα urls κρατά τις διευθύνσεις των εικόνων που θα επισημανθούν.
 * Η λίστα labels (λεξικό) κρατά τις ετικέτες επισήμανσης, για να γίνει επισήμανση των εικόνων
 * με τα μοντέλα YoloWorld και CLIPS. Η ιδιότητα threshold και topK κρατά το κατώφλι
 * και τις επικρατέστερες topK κλάσεις, στοιχεία που χρειάζονται κατά τη διαδικασία επισήμανσης.
 * Η ιδιότητα text κρατά το κείμενο που θα επισημανθεί. Έτσι, ανάλογα αν θα επισημανθεί
 * κείμενο ή εικόνα επιλέγεται κατάλληλος κατασκευαστής της κλάσης για να γίνει η
 * επισήμανση
 */
public class DataForAnnotation {
    private List<String> urls;
    private List<String> labels;
    private float threshold;;
    private int topK;
    private String text;
    private boolean ignoreSSL;

    /**
     * Κατασκευαστής κλάσης
     */
    public DataForAnnotation() {
    }

    /**
     * Κατασκευαστής για επισήμανση εικόνων
     * @param urls λίστα με τις διευθύνσεις των εικόνων
     * @param labels ετικέτες (λεξικό) για τα μοντέλα YoloWorld και CLIP
     * @param threshold κατώφλι
     * @param topK topk
     */
    public DataForAnnotation(List<String> urls, List<String> labels, float threshold, int topK, boolean ignoreSSL) {
        this.urls = urls;
        this.labels = labels;
        this.threshold = threshold;
        this.topK = topK;
        this.ignoreSSL = ignoreSSL;
    }

    /**
     * Κατασκευαστής για επισήμανση κειμένου
     * @param text
     */
    public DataForAnnotation(String text) {
        this.text = text;
    }

    /**
     * @return λίστα με τις διευθύνσεις των εικόνων που θα επισημειωθούν
     */
    public List<String> getUrls() {
        return urls;
    }

    /**
     * Αλλαγή της λίστας εικόνων
     * @param urls νέες εικόνες
     */
    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    /**
     * @return λίστα με ετικέτες για τα μοντέλα YoloWorld και CLIP
     */
    public List<String> getLabels() {
        return labels;
    }

    /**
     * Αλλαγή λίστας ετικετών
     * @param labels νέα λίστα με ετικέτες επισήμανσης
     */
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    /**
     * @return το κατώφλι
     */
    public float getThreshold() {
        return threshold;
    }

    /**
     * Αλλαγή του κατωφλιού επισήμανσης
     * @param threshold νέο κατώφλι
     */
    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    /**
     * @return επιστρέφει το topK
     */
    public int getTopK() {
        return topK;
    }

    /**
     * Αλλαγή του topK
     * @param topK νέο topK
     */
    public void setTopK(int topK) {
        this.topK = topK;
    }

    /**
     * @return κείμενο επισήμανσης
     */
    public String getText() {
        return text;
    }

    /**
     * Αλλαγή του κειμένου επισήμανσης
     * @param text κείμενο επισήμανσης
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     *
     * @return επιστρέφει την πρόθεση παράκαμψης του SSL
     */
    public boolean isIgnoreSSL() {
        return ignoreSSL;
    }

    /**
     * Αλλαγή της πρόθεσης παράκαμψης του SSL
     * @param ignoreSSL παράκαμψη του SSL
     */
    public void setIgnoreSSL(boolean ignoreSSL) {
        this.ignoreSSL = ignoreSSL;
    }
}
