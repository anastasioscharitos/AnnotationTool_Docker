package com.example.application.backEnd.dataHandling;

import com.example.application.backEnd.uploadData.ServiceData;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

/**
 * Η κλάση WebPageImage δημιουργεί αντικείμενα επισήμανσης εικόνων.
 * Υλοποιεί το BaseAnnotation και λαμβάνει έναν μοναδικό αριθμό σε μορφή id_image.
 * Η ιδιότητα imagePath κρατά το τη θέση αποθήκευσης της εικόνας.
 * Η ιδιότητα text κρατά τη λεζάντα της εικόνας ενώ οι λίστες annotation και userAnnotation
 * κρατούν λίστα από τις επισημάνσεις που έγιναν για την εικόνα από τα μοντέλα και
 * τον χρήστη, αντίστοιχα
 */
public class WebPageImage implements BaseAnnotation{

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private ServiceData serviceData;
    private String url;
    private String imagePath;
    private String text;
    private List<String> annotation;
    private List<String> userAnnotation;
    private final String type = "image";

    /**
     * Κατασκευαστής κλάσης
     * Απαραίτητος για τη διαδικασία την αποσειριοποίησης (deserialization)
     */
    public WebPageImage() {
    }
    /**
     * Κατασκευαστής κλάσης
     * @param url διεύθυνση σελίδας
     * @param imagePath θέση αποθήκευσης της εικόνας
     * @param text λεζάντα εικόνας
     * @param annotation λίστα με τις κλάσεις επισήμανσης των μοντέλων
     * @param userAnnotation λίστα με τις κλάσεις επισημάνσεων του χρήστη
     * @param serviceData ServiceData
     */
    public WebPageImage(String url, String imagePath, String text, List<String> annotation, List<String> userAnnotation, ServiceData serviceData) {
        this.serviceData = serviceData;
        this.id =  serviceData.getIdCounter() + "_image";
        serviceData.setIdCounter();
        this.url = url;
        this.imagePath = imagePath;
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
     * @return η λεζάντα της εικόνας
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
     * @return τον τύπο final image
     */
    @Override
    public String getType() {
        return type;
    }
    /**
     * Όταν δημιουργείται το αντικείμενο αποθηκεύεται η διεύθυνση της εικόνας.
     * Όταν τελικά αποθηκευτεί η εικόνα τότε αποθηκεύεται η θέση που βρίσκεται τοπικά
     * @return τη θέση αποθήκευσης της εικόνας
     */
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
     * Αλλαγή της θέση της εικόνας
     * @param imagePath νέα θέση της εικόνας
     */
    public void setImagePath(String imagePath){
        this.imagePath = imagePath;
    }
    /**
     * Αλλαγή της λεζάντας
     * @param text νέα λεζάντα
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
     * Κλάσεις για τη σύγκριση δύο αντικειμένων. H σύγκριση γίνεται με βάση το id
     * @param obj   the reference object with which to compare.
     * @return true αν έχουν ίδιο id
     */
    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }

        WebPageImage that = (WebPageImage) obj;

        Boolean bool = Objects.equals(id, that.id);

        return bool;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
