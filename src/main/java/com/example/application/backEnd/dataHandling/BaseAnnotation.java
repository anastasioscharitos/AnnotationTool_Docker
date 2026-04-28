package com.example.application.backEnd.dataHandling;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;

/**
 * Η κλάση BaseAnnotation αποτελεί το interface για όλους τους τύπους επισημάνσεων
 * WebPageText, WebPageImage, WebPageMergeNew, WebPageImageAddText.
 * Υλοποιεί το Serializable για μεταφορά δεδομένων και το Comparable για τη σύγκριση
 * αντικειμένων BaseAnnotation με βάση το id
 * Χρησιμοποιεί Jackson annotations για τον χειρισμό του πολυμορφισμού. Κατά τη μετατροπή
 * προστίθεται μια επιπλέον ιδιότητα "info" που υποδεικνύει τον τύπο της κλάσης
 * επιτρέποντας τη σωστή αποσειριοποίηση (deserialization)
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "info"
)

@JsonSubTypes({
        @JsonSubTypes.Type(value = WebPageText.class, name = "text"),
        @JsonSubTypes.Type(value = WebPageImage.class, name = "image"),
        @JsonSubTypes.Type(value = WebPageMergeNew.class, name = "merge"),
        @JsonSubTypes.Type(value =  WebPageImageAddText.class, name = "imageAddText")}
)

public interface BaseAnnotation extends Serializable, Comparable<BaseAnnotation> {

    public String getUrl();

    public String getText();

    public List<String> getAnnotation();

    public List<String> getUserAnnotation();
    public String getType();
    public String getImagePath();

    public void setAnnotation(List<String> annotation);

    public void setUserAnnotation(List<String> userAnnotation);

    public  String getId();


    /**
     * Συγκρίνει τα αντικείμενα BaseAnnotation με βάση το id τους. Το id αποτελείται
     * από έναν αριθμό, κάτω παύλα και τον τύπο επισήμανσης. Για τη σύγκριση λαμβάνεται
     * υπόψη μόνο ο αριθμός
     * @param o αντικείμενο BaseAnnotation που θα εξεταστεί
     * @return 0 αν τα αντικείμενα έχουν ίδιο id και 1 ή -1 αν είναι διαφορετικά
     */
    @Override
    default int compareTo(BaseAnnotation o) {
        int thisNum = Integer.parseInt(this.getId().split("_")[0]);
        int otherNum = Integer.parseInt(o.getId().split("_")[0]);
        return Integer.compare(thisNum, otherNum);
    }
}
