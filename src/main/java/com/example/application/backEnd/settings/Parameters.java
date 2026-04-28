package com.example.application.backEnd.settings;


import com.example.application.backEnd.uploadData.ServiceData;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Η κλάση Parameters διαχειρίζεται τις παραμέτρους και τις προτιμήσεις του χρήστη. Η κλάση είναι
 * @Component και @VaadinSessionScope δημιουργείται ένα στιγμιότυπο για κάθε ενεργή συνεδρία χρήστη.
 * Περιλαμβάνει ρυθμίσεις για τα μοντέλα NLP (spaCy, Stanford CoreNLP) και για τα μοντέλα
 * Yolo, Yolo World, κα CLIPS. Επίσης, περιλαμβάνει τεχνικές παραμέτρους για το scraping
 * και την επισήμανση. Τέλος, υπάρχει προτίμηση για τη γλώσσα της εφαρμογής.
 */
@Component
@VaadinSessionScope
public class Parameters implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;

    private int topK = 3;
    private float threshold = 0.30F;
    private boolean spacy = true;
    private boolean stanfordCoreNLP = false;

    private boolean yolo = true;
    private boolean clip = false;
    private boolean yoloWorld = false;
    private String lang = "el";
    private boolean deepScraping = false;
    private boolean ignoreSSL = false;
    private List<String> listLabelSpacy = LabelsSpaCy.getKeys();
    private List<String> listLabelYolo = LabelsYolo.getKeys();
    private ServiceData serviceData;

    /**
     * Κατασκευαστής κλάσης
     */
    public  Parameters(){}

    /**
     * Κατασκευαστής κλάσης, παράμετρο ServiceData η οποία είναι @Service
     * @param serviceData
     */
    public Parameters(ServiceData serviceData) {
        this.serviceData = serviceData;
    }

    /**
     * Κατασκευαστής κλάσης
     * @param serviceData
     * @param topK
     * @param threshold
     * @param spacy
     * @param stanford
     * @param yolo
     * @param clip
     * @param yoloWorld
     * @param lang
     * @param deepScraping
     * @param ignoreSSL
     * @param listLabelSpacy
     * @param listLabelYolo
     */
    public Parameters(ServiceData serviceData,  int topK, float threshold, boolean spacy, boolean stanford, boolean yolo, boolean clip,
                      boolean yoloWorld, String lang, boolean deepScraping, boolean ignoreSSL, List<String> listLabelSpacy, List<String> listLabelYolo) {
        this.serviceData = serviceData;

        this.topK = topK;
        this.threshold = threshold;
        this.spacy = spacy;
        this.stanfordCoreNLP = stanford;

        this.lang = lang;
        this.deepScraping = deepScraping;
        this.ignoreSSL = ignoreSSL;
        this.yolo = yolo;
        this.clip = clip;
        this.yoloWorld = yoloWorld;
        this.listLabelSpacy = listLabelSpacy;
        this.listLabelYolo = listLabelYolo;
    }

    /**
     * @return το topK των επισήμανσης
     */
    public int getTopK() {
        return topK;
    }

    /**
     * Αλλάζει το topK του μοντέλου επισήμανσης
     * @param topK
     */
    public void setTopK(int topK) {
        this.topK = topK;
    }

    /**
     * @return το κατώφλι του μοντέλου
     */
    public float getThreshold() {
        return threshold;
    }

    /**
     * Αλλάζει το κατώφλι του μοντέλου
     * @param threshold
     */
    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    /**
     * @return λίστα με τις κλάσεις του μοντέλου spaCy
     */
    public List<String> getListLabelSpacy() {
        return listLabelSpacy;
    }

    /**
     * @return λίστα με τις κλάσεις του μοντέλου yolo
     */
    public List<String> getListLabelYolo() {
        return listLabelYolo;
    }

    /**
     * @return true αν χρησιμοποιείται το μοντέλο spaCy διαφορετικά false
     */
    public boolean getSpacy() {
        return spacy;
    }

    /**
     * Επιστρέφει το μοντέλο επισήμανσης κειμένου
     * @return
     */
    public String getTextModel(){
        if(spacy){
            return "spaCy";
        }
            return "StanfordCoreNLP";
    }

    /**
     * Αλλαγή της κατάστασης του μοντέλου που χρησιμοποιείται για επισήμανση κειμένου.
     * Αν το μοντέλο δεν είναι το spaCy τότε γίνεται false
     * @param str το μοντέλο επισήμανσης κειμένου
     */
    public void setSpacy(String str) {
        if(str.equals("spaCy")){
            this.spacy = true;
        }
        else{
            this.spacy = false;
        }
    }

    /**
     * @return true αν χρησιμοποιείται το μοντέλο StanfordCoreNLP
     */
    public boolean getStanfordCoreNLP() {
        return stanfordCoreNLP;
    }

    /**
     * Αν το μοντέλο που χρησιμοποιείται για επισήμανση κειμένου δεν είναι το
     * StanfordCoreNLP τότε γίνεται false
     * @param str
     */
    public void setStanfordCoreNLP(String str) {
        if(str.equals("StanfordCoreNLP")){
            this.stanfordCoreNLP = true;
        }
        else{
            this.stanfordCoreNLP = false;
        }
    }

    /**
     * @return true αν το μοντέλο επισήμανσης κειμένου είναι το Yolo
     */
    public boolean getYolo() {
        return yolo;
    }

    /**
     * Αν το μοντέλο επισήμανσης εικόνας δεν είναι το yolo τότε έχει την τιμή false
     * @param str μοντέλο επισήμανσης εικόνας
     */
    public void setYolo(String str) {
        if(str.equals("YOLO")){
            this.yolo = true;
        }
        else{
            this.yolo = false;
        }
    }

    /**
     * @return true αν το μοντέλο επισήμανσης εικόνας είναι το CLIP
     */
    public boolean getClip() {
        return clip;
    }

    /**
     * Αν το μοντέλο επισήμανσης εικόνας δεν είναι το Clip τότε έχει την τιμή false
     * @param str μοντέλο επισήμανσης εικόνας
     */
    public void setClip(String str) {
        if(str.equals("CLIP")){
            this.clip = true;
        }
        else{
            this.clip = false;
        }
    }

    /**
     * @return true αν το μοντέλο επισήμανσης εικόνας είναι το yolo world
     */
    public boolean getYoloWorld(){
        return yoloWorld;
    }

    /**
     * Αν το μοντέλο επισήμανσης εικόνας δεν είναι το Yolo World τότε έχει την τιμή false
     * @param str μοντέλο επισήμανσης εικόνας yolo world
     */
    public void setYoloWorld(String str){
        if(str.equals("YOLO_WORLD")){
            this.yoloWorld = true;
        }
        else{
            this.yoloWorld = false;
        }
    }

    /**
     * @return String το μοντέλο επισήμανσης εικόνας
     */
    public String getImageModel(){
        if(yolo){
            return "YOLO";
        }else if(clip) {
            return "CLIP";
        }
        else {
            return "YOLO_WORLD";
        }
    }

    /**
     * Αλλαγή μοντέλου επισήμανσης εικόνας
     * @param model μοντέλο επισήμανση
     */
    public void setImageModel(String model){
        setYolo(model);
        setYoloWorld(model);
        setClip(model);
    }

    /**
     * @return τη γλώσσα της εφαρμογής
     */
    public String getLang(){
        return lang;
    }

    /**
     * Αλλαγή γλώσσας εφαρμογής
     * @param lang νέα γλώσσα εφαρμογής
     */
    public void setLang(String lang){
        this.lang = lang;
    }

    /**
     * Αν ο χρήστης θα κάνει scraping σε μη στατικές σελίδες
     * @return true αν ο χρήστης θα χρησιμοποιήσει το PlayWright για να κάνει scraping
     */
    public boolean isDeepScraping() {
        return deepScraping;
    }

    /**
     * Αλλαγή του τρόπου που θα γίνει το scraping στις σελίδες
     * @param deepScraping true αν θα πραγματοποιηθεί με το PlayWright διαφορετικά false
     */
    public void setDeepScraping(boolean deepScraping) {
        this.deepScraping = deepScraping;
    }

    /**
     * Αν θα γίνει παράκαμψη του πιστοποιητικού SSL σε σελίδες HTTPS
     * @return true αν θα γίνει παράκαμψη του SSL διαφορετικά false
     */
    public boolean isIgnoreSSL() {
        return ignoreSSL;
    }

    /**
     * Αλλαγή της παράκαμψης του πιστοποιητικού SSL
     * @param ignoreSSL true αν θα γίνει παράκαμψη του SSL διαφορετικά false
     */
    public void setIgnoreSSL(boolean ignoreSSL) {
        this.ignoreSSL = ignoreSSL;
    }
}
