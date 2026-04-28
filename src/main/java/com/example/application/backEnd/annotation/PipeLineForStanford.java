package com.example.application.backEnd.annotation;


import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Η κλάση PipeLineForStanford είναι υπεύθυνη για τη ρύθμιση και αρχικοποίηση του
 * Stanford CoreNLP. Παρέχει τις απαραίτητες ιδιότητες (Properties) για την ανάλυση
 * κειμένου στη java και χρησιμοποιείται για την αναγνώριση οντοτήτων (Named Entity
 * Recognition NER).
 * Η επισήμανση γίνεται μέσω της java.
 */
@Service
public class PipeLineForStanford {
    private StanfordCoreNLP pipeline;

    /**
     * Κατασκευαστής
     */
    public PipeLineForStanford(){

    }
    /**
     * Αρχικοποιεί το pipeline (αν δεν έχει ήδη αρχικοποιηθεί). Είναι synchronized για να
     * μην πραγματοποιηθούν πολλές αρχικοποιήσεις.
     * Ορίζει τους annotators tokenize, ssplit, pos, lemma, ner.
     */
    public synchronized void init(){
        if(pipeline == null){
            Properties properties = new Properties();
            properties.setProperty("annotators" , "tokenize, ssplit, pos, lemma, ner");
            this.pipeline = new StanfordCoreNLP(properties);
        }
    }

    /**
     * @return pipeline αν έχει αρχικοποιηθεί διαφορετικά null
     */
    public StanfordCoreNLP getPipeline() {
        return pipeline;
    }

    /**
     * Αλλαγή του pipeline
     * @param pipeline το νέο pipeline
     */
    public void setPipeline(StanfordCoreNLP pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * Ελέγχει αν το pipeline έχει φορτωθεί
     * @return true αν το pipeline έχει αρχικοποιηθεί, διαφορετικά false
     */
    public  boolean isLoaded(){
        return pipeline != null;
    }
}
