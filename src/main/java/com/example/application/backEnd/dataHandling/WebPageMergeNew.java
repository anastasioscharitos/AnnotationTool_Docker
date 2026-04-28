package com.example.application.backEnd.dataHandling;

import com.example.application.backEnd.uploadData.ServiceData;

import java.io.Serial;
import java.util.*;

/**
 * Η κλάση WebPageMergeNew δημιουργεί αντικείμενα από συγχώνευση άλλων αντικειμένων επισήμανσης.
 * Υλοποιεί το BaseAnnotation και λαμβάνει έναν μοναδικό αριθμό σε μορφή id_mergeNew. Η ιδιότητα imagePath κρατά τη θέση
 * αποθήκευσης της εικόνας. Η ιδιότητα text είναι "", ενώ οι λίστες annotation και userAnnotation
 * κρατούν λίστα από το σύνολο των επισημάνσεων που έγιναν από τα μοντέλα και τον χρήστη, αντίστοιχα.
 */
public class WebPageMergeNew implements BaseAnnotation{

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private ServiceData serviceData;
    private List<BaseAnnotation> baseAnnotationList;
    private String url;
    private  String imagePath;
    private List<String> annotation;
    private List<String> userAnnotation;
    private final String type = "mergeNew";
    private String text = "";
    /**
     * Κατασκευαστής κλάσης
     * Απαραίτητος για τη διαδικασία την αποσειριοποίησης (deserialization)
     */
    public WebPageMergeNew(){}
    /**
     * Κατασκευαστής κλάσης
     * @param baseAnnotationList λίστα με τα αντικείμενα επισήμανσης που συγχωνεύονται
     * @param serviceData ServiceData
     */
    public WebPageMergeNew(List<BaseAnnotation> baseAnnotationList, ServiceData serviceData){

        this.serviceData = serviceData;
        this.id = serviceData.getIdCounter()+"_mergeNew";
        serviceData.setIdCounter();
        this.baseAnnotationList = new ArrayList<>(baseAnnotationList);
        this.annotation = new ArrayList<>();
        this.userAnnotation = new ArrayList<>();
        this.imagePath = getImagePath();
        this.url = getUrl();
        this.text = getText();
    }

    /**
     * @return λίστα με τα αντικείμενα που έχουν συγχωνευτεί
     */
    public List<BaseAnnotation> getBaseAnnotationList(){
        return baseAnnotationList;
    }

    /**
     * @return τη διεύθυνση του πρώτου αντικειμένου τις λίστας των συγχωνευμένων αντικειμένων
     */
    @Override
    public String getUrl(){
        if(baseAnnotationList == null || baseAnnotationList.isEmpty()){
            return "";
        }
        return baseAnnotationList.getFirst().getUrl();
    }

    /**
     * @return το κείμενο από κάθε ένα από τα αντικείμενο που συγχωνεύτηκαν σε ένα
     * String
     */
    @Override
    public String getText(){
        if(baseAnnotationList == null || baseAnnotationList.isEmpty()){
            return "";
        }
        int counter = 1;
        StringBuilder builder = new StringBuilder();

        for(BaseAnnotation base : baseAnnotationList){

            builder.append(counter);
            builder.append(") ");
            builder.append(base.getText());
            builder.append(", ");
            counter++;
        }
        return builder.toString();
    }

    /**
     * Αν δεν έχουν συγχωνευτεί αντικείμενα τότε επιστρέφει new ArrayList.
     * Αν η λίστα επισήμανσης δεν είναι άδεια τότε την επιστρέφει, διαφορετικά
     * επιστρέφει λίστα με τις επισημάνσεις όλων των συγχωνευμένων αντικειμένων
     * Η συγχωνευμένη λίστα επισημάνσεων είναι ταξινομημένη και περιέχει μόνο
     * μία φορά την κάθε ετικέτα από τις επιμέρους επισημάνεις
     * @return λίστα επισημάνσεων μοντέλου
     */
    @Override
    public List<String> getAnnotation() {
        if(baseAnnotationList == null || baseAnnotationList.isEmpty()){
            return new ArrayList<>();
        }

        Map<String, String> map = new HashMap<>();
        Set<String> annotationTreeSet = new TreeSet<>();

        for(BaseAnnotation base : baseAnnotationList){
            if(base instanceof WebPageMergeNew){
                addWebMerge(map, base);
            }
            else{
                addTags(map, base);
            }
        }

        for(Map.Entry<String, String> entry : map.entrySet()){
            String str = entry.getKey() + " : " + entry.getValue();
            annotationTreeSet.add(str);
        }

        annotation = new ArrayList<>(annotationTreeSet);
        return  annotation;
    }

    /**
     * Βοηθητική μέθοδος για να προσθέσει τις ετικέτες σε ένα ενιαίο χάρτη. Λόγω
     * ότι οι ετικέτες από την επισήμανση κειμένου και εικόνας έχουν διαφορετική μορφή,
     * πρέπει όλες να συγκεντρωθούν στην ίδια ετικέτα, πχ όλες οι επισημάνεις στο
     * person πρέπει να συγκεντρωθούν κάτω από την ίδια ετικέτα.
     * @param map χάρτης με κλειδί την ετικέτα και value τιμή που επέστρεψε τα μοντέλα
     *            ή για την περίπτωση της εικόνας "Annotation for Image"
     * @param base αντικείμενο BaseAnnotation
     */
    private void addTags(Map<String,String> map , BaseAnnotation base){
        for(String item : base.getAnnotation()){
            if(item.contains(":")) {
                String[] parts = item.split(":", 2);
                String tag = parts[0].trim();
                String value = parts[1].trim();
                map.merge(tag, value, (oldVal, newVal) -> oldVal + ", " + newVal);
            }
            else{
                map.merge(item.toUpperCase(), "Annotation from Image", (oldVal, newVal) -> oldVal + ", " + newVal);
            }
        }
    }

    /**
     * Βοηθητική μέθοδος εκτελείται αναδρομικά μέχρι να βρει αντικείμενα WebPageText,
     * WebPageImage ή WebPageImageAddText για να βρει τις ετικέτες.
     * @param map χάρτης με κλειδί την ετικέτα και value τιμή που επέστρεψε τα μοντέλα
     *                 ή για την περίπτωση της εικόνας "Annotation for Image"
     * @param base αντικείμενο BaseAnnotation
     */
    private void addWebMerge(Map<String, String> map, BaseAnnotation base){
        WebPageMergeNew temp = (WebPageMergeNew) base;
        for(BaseAnnotation baseAnnotation : temp.baseAnnotationList) {
            if(baseAnnotation instanceof WebPageMergeNew){
                addWebMerge(map, baseAnnotation);
            }else{
                addTags(map, baseAnnotation);
            }
        }
    }

    /**
     * Αν δεν έχουν συγχωνευτεί αντικείμενα τότε επιστρέφει new ArrayList.
     * Αν η λίστα επισήμανσης χρήστη δεν είναι άδεια τότε την επιστρέφει, διαφορετικά
     * επιστρέφει λίστα με τις επισημάνσεις χρηστών όλων των συγχωνευμένων αντικειμένων
     * @return λίστα επισημάνσεων μοντέλου
     */
    @Override
    public List<String> getUserAnnotation() {
        return userAnnotation;
    }
    /**
     * @return τον τύπο final mergeNew
     */
    @Override
    public String getType(){
        return type;
    }

    /**
     * Επιστρέφει την πρώτη εικόνα που θα βρεί από τα συγχωνευμένα
     * αντικείμενα. Αν δεν υπάρχουν αντικείμενα που περιέχουν εικόνα
     * τότε επιστρέφει ""
     * @return το μονοπάτι αποθήκευσης της εικόνας
     */
    @Override
    public String getImagePath(){

        if(baseAnnotationList == null || baseAnnotationList.isEmpty()){
            return null;
        }

        for(BaseAnnotation baseAnnotation : baseAnnotationList){

            if(baseAnnotation instanceof  WebPageImage){
                return baseAnnotation.getImagePath();
            }
            else if (baseAnnotation instanceof WebPageImageAddText) {
                return baseAnnotation.getImagePath();
            }
        }
        return "";
    }
    /**
     * Αλλαγή της λίστας επισημάνσεων
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
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }

        WebPageMergeNew that = (WebPageMergeNew) obj;

        Boolean bool = Objects.equals(id, that.id);

        return bool;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
