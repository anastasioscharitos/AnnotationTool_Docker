package com.example.application.backEnd.annotation;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.ArrayList;
import java.util.List;

/**
 * Η κλάση StanfordCoreAnnotate είναι υπεύθυνη για την εξαγωγή ονοματισμένων οντοτήτων
 * (Named Entity Recognition NER). Χρησιμοποιεί το pipeline του Stanford CoreNLP
 * για τον εντοπισμό οντοτήτων
 */
public class StanfordCoreAnnotate {
    /**
     * Κατασκευαστής
     */
    public StanfordCoreAnnotate(){}

    /**
     *Η μέθοδος getStanfordCoreAnnotate εκτελεί την ανάλυση κειμένου και επιστρέφει
     * μια λίστα με τις οντότητες που εντόπισε.
     * @param pipelineService το ενεργό pipeline
     * @param text το κείμενο που θα αναλυθεί
     * @return μια λίστα με οντότητες. Κάθε εγγραφή στη λίστα έχει τη μορφή {Οντότητα : κείμενο}
     */
    public List<String> getStanfordCoreAnnotate(StanfordCoreNLP pipelineService, String text){

        CoreDocument coreDocument = new CoreDocument(text);
        pipelineService.annotate(coreDocument);

        List<CoreEntityMention> coreLabels = coreDocument.entityMentions();

        List<String> result = new ArrayList<>();
        for(CoreEntityMention entity : coreLabels){

            String ner = entity.entityType();
            String entityTxt = entity.text();
            result.add(ner + ": " + entityTxt);
        }
        return result;
    }
}
