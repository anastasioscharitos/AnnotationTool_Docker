package com.example.application.backEnd.uploadData;

import com.example.application.backEnd.dataHandling.WebPageText;

import java.util.Objects;

/**
 * Η κλάση Word αντιπροσωπεύει μια ετικέτα επισήμανσης. Η κλάση Word
 * κάνει implements στο Comparable για να μπορέσει να γίνει η σύγκριση και να ταξινομηθούν
 * τα στοιχεία κατά αλφαβητική σειρά
 */
public class Word implements Comparable<Word>{

    private String word;

    public Word(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    /**
     * Μέθοδοι για τη σύγκριση δύο αντικειμένων Word
     * @param obj   the reference object with which to compare.
     * @return 0 για ίδια αντικείμενο 1,-1μεγαλύτερο και μικρότερο αντίστοιχα
     */
    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj  == null || getClass() != obj.getClass()){
            return false;
        }

        Word that = (Word) obj;

        Boolean bool = Objects.equals(word, that.word);

        return bool;
    }

    @Override
    public int hashCode() {
        return Objects.hash(word);
    }

    @Override
    public int compareTo(Word o) {
        return this.word.compareToIgnoreCase(o.word);
    }
}
