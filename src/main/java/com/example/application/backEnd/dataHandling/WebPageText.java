package com.example.application.backEnd.dataHandling;

import com.example.application.backEnd.uploadData.ServiceData;

import java.io.Serial;
import java.util.List;
import java.util.Objects;
/**
 * Η κλάση WebPageText δημιουργεί αντικείμενα επισήμανσης κειμένου.
 * Υλοποιεί το BaseAnnotation και λαμβάνει έναν μοναδικό αριθμό σε μορφή id_text.
 * Η ιδιότητα imagePath κρατά το "", δεν υπάρχει εικόνα.
 * Η ιδιότητα text κρατά το κείμενο που θα επισημανθεί ενώ οι λίστες annotation και userAnnotation
 * κρατούν λίστα από τις επισημάνσεις που έγιναν για την εικόνα από τα μοντέλα και
 * τον χρήστη, αντίστοιχα
 */
public class WebPageText implements BaseAnnotation{

    @Serial
    private static final long serialVersionUID = 1L;

    private ServiceData serviceData;

    private String id;
    private String url;
    private String imagePath;
    private String text;
    private List<String> annotation;
    private List<String> userAnnotation;
    private final String type = "text";

    /**
     * Κατασκευαστής κλάσης
     * Απαραίτητος για τη διαδικασία την αποσειριοποίησης (deserialization)
     */
    public WebPageText() {
    }
    /**
     * Κατασκευαστής κλάσης
     */
    public WebPageText(ServiceData serviceData) {
        this.serviceData = serviceData;
    }
    /**
     * Κατασκευαστής κλάσης
     * @param url διεύθυνση σελίδας
     * @param text λεζάντα εικόνας
     * @param annotation λίστα με τις κλάσεις επισήμανσης των μοντέλων
     * @param userAnnotation λίστα με τις κλάσεις επισημάνσεων του χρήστη
     * @param serviceData ServiceData
     */
    public WebPageText(String url, String text, List<String> annotation, List<String> userAnnotation, ServiceData serviceData) {
        this.serviceData = serviceData;
        this.id =  serviceData.getIdCounter() + "_text";
        serviceData.setIdCounter();
        this.url = url;
        this.imagePath = "";
        this.text = text;
        this.annotation = annotation;
        this.userAnnotation = userAnnotation;
    }

    /**
     * @return η διεύθυνση της σελίδας
     */
    @Override
    public String getUrl() {
        return url;
    }
    /**
     * @return κείμενο επισήμανσης
     */
    @Override
    public String getText() {
        return text;
    }
    /**
     * @return λίστα με τις κλάσεις επισήμανσης που έκανε το μοντέλο
     */
    @Override
    public List<String> getAnnotation() {
        return annotation;
    }
    /**
     * @return λίστα με τις κλάσεις επισήμανσης που έκανε ο χρήστης
     */
    @Override
    public List<String> getUserAnnotation() {
        return userAnnotation;
    }
    /**
     * @return τον τύπο final text
     */
    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getImagePath(){
        return imagePath;
    }
    /**
     * Αλλαγή της διεύθυνσης της σελίδας με νέα διεύθυνση
     * @param url νέα διεύθυνση
     */
    public void setUrl(String url) {
        this.url = url;
    }
    /**
     * Αλλαγή του κειμένου επισήμανσης
     * @param text νέα κείμενο επισήμανσης
     */
    public void setText(String text) {
        this.text = text;
    }
    /**
     * Αλλαγή της λίστας επισημάνσεων της εικόνας
     * @param annotation νέα λίστα επισημάνσεων
     */
    @Override
    public void setAnnotation(List<String> annotation) {
        this.annotation = annotation;
    }
    /**
     * Αλλαγή της λίστα επισημάνσεων του χρήστη
     * @param userAnnotation νέα λίστα επισημάνσεων
     */
    @Override
    public void setUserAnnotation(List<String> userAnnotation) {
        this.userAnnotation = userAnnotation;
    }
    /**
     * @return τον μοναδικό αριθμό id του αντικειμένου
     */
    @Override
    public String getId(){
        return id;
    }
    /**
     * Κλάσεις για τη σύγκριση δύο αντικειμένων. η σύγκριση γίνεται με βάση το id
     * @param obj   the reference object with which to compare.
     * @return true αν έχουν ίδιο id
     */
    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj  == null || getClass() != obj.getClass()){
            return false;
        }

        WebPageText that = (WebPageText) obj;

        Boolean bool = Objects.equals(id, that.id);

        return bool;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
