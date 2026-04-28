package com.example.application.backEnd.dataHandling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Αυτή η κλάση αποτελείται λειτουργεί ως ένας Container για τους τύπος επισήμανσης
 * που αντιστοιχούν σε μια σελίδα. Υπάρχουν τέσσερις λίστες με επισημάνσεις κειμένου, εικόνας,
 * εικόνας με κείμενο και συγχώνευση επισημάνσεων
 */
public class AnnotatedData {

    private String pageUrl;
    private List<WebPageText> textList;
    private List<WebPageImage> imageList;
    private List<WebPageMergeNew> mergeList;
    private List<WebPageImageAddText> imageAddTextList;

    /**
     * Κατασκευαστής κλάσης
     */
    public AnnotatedData(){
        this.pageUrl = "";
        this.textList = new ArrayList<>();
        this.imageList = new ArrayList<>();
        this.mergeList = new ArrayList<>();
        this.imageAddTextList = new ArrayList<>();
    }

    /**
     * @return τη διεύθυνση url της σελίδας
     */
    public String getPageUrl() {
        return pageUrl;
    }

    /**
     * Αλλαγή της διεύθυνσης της σελίδας. Κατά την αρχικοποίηση η διεύθυνση
     * είναι isBlank()
     * @param pageUrl νέα διεύθυνση σελίδας
     */
    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    /**
     * @return λίστα με αντικείμενο WebPageText, επισημειώσεις κειμένου
     */
    public List<WebPageText> getTextList() {
        return textList;
    }


    public void setTextList(List<WebPageText> textList) {
        this.textList = textList;
    }

    /**
     * @return επιστρέφει λίστα με αντικείμενο WebPageImage, επισημειώσεις εικόνων
     */
    public List<WebPageImage> getImageList() {
        return imageList;
    }


    public void setImageList(List<WebPageImage> imageList) {
        this.imageList = imageList;
    }

    /**
     * @return επιστρέφει λίστα με αντικείμενα WebPageMergeNew, αντικείμενα
     * συγχώνευσης επισημειώσεων
     */
    public List<WebPageMergeNew> getMergeList() {
        return mergeList;
    }

    public void setMergeList(List<WebPageMergeNew> mergeList) {
        this.mergeList = mergeList;
    }

    /**
     * @return επιστρέφει λίστα με αντικείμενα WebPageImageAddText, αντικείμενα
     *επισημειώσεων με κείμενο
     */
    public List<WebPageImageAddText> getImageAddTextList() {
        return imageAddTextList;
    }

    public void setImageAddTextList(List<WebPageImageAddText> imageAddTextList) {
        this.imageAddTextList = imageAddTextList;
    }

    /**
     * Η μέθοδος convertDataToBase μετασχηματίζει τα δεδομένα από AnnotatedData
     * σε BaseAnnotation. Γίνεται η συγκέντρωση όλων των αντικειμένων (διάφοροι τύποι)
     * και σχηματίζεται μια πολυμορφική λίστα αντικειμένων
     * @param data, λίστα αντικείμενα με AnnotatedData
     * @return λίστα με αντικείμενα BaseAnnotation
     */
    public List<BaseAnnotation> convertDataToBase(List<AnnotatedData> data){
        List<BaseAnnotation> baseAnnotations = new ArrayList<>();

        for(AnnotatedData d : data){
            if(d.textList != null && !d.textList.isEmpty()){
                for(WebPageText txt : d.textList){
                    baseAnnotations.add(txt);
                }
            }
            if(d.imageList != null && !d.imageList.isEmpty()){
                for(WebPageImage img : d.imageList){
                    baseAnnotations.add(img);
                }
            }
            if(d.mergeList != null && !d.mergeList.isEmpty()){
                for(WebPageMergeNew textAndImage : d.mergeList){
                    baseAnnotations.add(textAndImage);
                }
            }
            if(d.imageAddTextList!= null && !d.imageAddTextList.isEmpty()){
                for(WebPageImageAddText imageAddText: d.imageAddTextList){
                    baseAnnotations.add(imageAddText);
                }
            }
        }
        return baseAnnotations;

    }

    /**
     * Προσθήκη μιας λίστας με αντικείμενα WebPageText στη λίστα που περιέχει
     * τις επισημειώσεις κειμένου.
     * @param textList λίστας με αντικείμενα WebPageText
     */
    public void addTextList(List<WebPageText> textList){
        this.textList.addAll(textList);
    }

    /**
     * Προσθήκη μιας λίστας με αντικείμενα WebPageImage στη λίστα που περιέχει
     * τις επισημειώσεις εικόνων
     * @param imageList λίστας με αντικείμενα WebPageImage
     */
    public void addImageList(List<WebPageImage> imageList){
        this.imageList.addAll(imageList);
    }
    /**
     * Προσθήκη μιας λίστας με αντικείμενα WebPageMergeNew στη λίστα που περιέχει
     * τις συγχωνεύσεις επισημειώσεων
     * @param textAndImage λίστας με αντικείμενα WebPageMergeNew
     */
    public void addTextAndImageList(List<WebPageMergeNew> textAndImage){
        this.mergeList.addAll(textAndImage);
    }
    /**
     * Προσθήκη μιας λίστας με αντικείμενα WebPageImageAddText στη λίστα που περιέχει
     * τις επισημειώσεις εικόνων με κείμενο
     * @param imageAddTextList λίστας με αντικείμενα WebPageImageAddText
     */
    public void addImageAddTextList(List<WebPageImageAddText> imageAddTextList){
        this.imageAddTextList.addAll(imageAddTextList);
    }

    /**
     * Προσθήκη ενός στοιχείο BaseAnnotation στην κατάλληλη λίστα (WebPageText,
     * WebPageImage, WebPageMergeNew, WebPageImageAddText).
     * Αν το αντικείμενο υπάρχει τότε αφαιρείται και προστίθεται το νέο.
     * @param baseAnnotation αντικείμενο BaseAnnotation
     */
    public void addItem(BaseAnnotation baseAnnotation) {

        if (baseAnnotation instanceof WebPageText webPageText) {
            if(!this.textList.contains(webPageText)) {
                this.textList.add(webPageText);
            }
            else{
                this.textList.remove(webPageText);
                this.textList.add(webPageText);
            }
        }
        if (baseAnnotation instanceof WebPageImage webPageImage) {
            if(!this.imageList.contains(webPageImage)){
                this.imageList.add(webPageImage);
            }
            else{
                this.imageList.remove(webPageImage);
                this.imageList.add(webPageImage);
            }
        }
        if (baseAnnotation instanceof WebPageMergeNew webPageMerge) {
            if (!this.mergeList.contains(webPageMerge)) {
                this.mergeList.add(webPageMerge);
            } else {
                this.mergeList.remove(webPageMerge);
                this.mergeList.add(webPageMerge);
            }
        }

            if (baseAnnotation instanceof WebPageImageAddText imageAddText) {
                if (!this.imageAddTextList.contains(imageAddText)) {
                    this.imageAddTextList.add(imageAddText);
                } else {
                    this.imageAddTextList.remove(imageAddText);
                    this.imageAddTextList.add(imageAddText);
                }
            }
        }

    /**Εξετάζεται ένα αντικείμενο BaseAnnotation σε ποια κλάση ανήκει (WebPageText,
     * WebPageImage, WebPageMergeNew, WebPageImageAddText)
     * και το αφαιρεί.
     * @param baseAnnotation
     */
    public void removeItem(BaseAnnotation baseAnnotation){

        if (baseAnnotation instanceof WebPageText webPageText) {
            this.textList.remove(webPageText);
        }
        if (baseAnnotation instanceof WebPageImage webPageImage) {
            this.imageList.remove(webPageImage);
        }
        if (baseAnnotation instanceof WebPageMergeNew webPageMergeNew) {
            this.mergeList.remove(webPageMergeNew);

        }
        if (baseAnnotation instanceof WebPageImageAddText imageAddText) {
            this.imageAddTextList.remove(imageAddText);

        }
    }
}
