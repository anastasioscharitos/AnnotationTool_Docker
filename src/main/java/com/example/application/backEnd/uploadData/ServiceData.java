package com.example.application.backEnd.uploadData;

import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.handledFiles.UploadSaveData;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *Η κλάση ServiceData διαχειρίζεται τα δεδομένα σε μια συνεδρία χρήστη (@VaadinSessionScope).
 * Σκοπός της κλάσης είναι να κρατά το idCounter, το μοναδικό αριθμό κάθε επισήμανσης,
 * καθώς και τις λίστες των διευθύνσεων και του λεξικού.
 */

@Component
@VaadinSessionScope
public class ServiceData {
    private final HandledProjects handledProjects;

    private String path;
    private int idCounter;

    private List<Url> urlAddress = new ArrayList<>();
    private List<Word> words  = new ArrayList<>();
    private UploadSaveData uploadSaveData;

    /**
     * Κατασκευαστής κλάσης
     * @param handledProjects
     */
    public ServiceData(HandledProjects handledProjects){
        this.handledProjects = handledProjects;
    }

    /**
     * Αρχικοποίηση των δεδομένων του project. Καθορίζει τις διαδρομές των αρχείων,
     * φορτώνει τον τελευταίο καταγεγραμμένο id και ανακτά τις λίστες των διευθύνσεων
     * και των ετικετών
     */
    public boolean initProjectData(){

        boolean hasInitProject = false;
        this.path = handledProjects.getPathMetadata() + "metadata_counter.txt";
        this.idCounter = counterInitialization();
        if(idCounter != -1) {
            this.uploadSaveData = new UploadSaveData(handledProjects);

            uploadSaveData.initUploadSaveData();
            this.urlAddress = uploadSaveData.loadUrls();
            this.words = uploadSaveData.loadWord();

            hasInitProject = true;
            System.out.println("Αρχικοποιήθηκε το ServiceData για το project " + handledProjects.getProjectName());
        }
        return hasInitProject;

    }

    /**
     * @return λίστας διευθύνσεων, αντικείμενα Url
     */
    public List<Url> getUrlAddress(){
        return urlAddress;
    }

    /**
     * @return λίστας διευθύνσεων, οι διευθύνσεις σε String
     */
    public List<String> getStringURLAddress(){
        List<String> list = new ArrayList<>();

        for(Url url: urlAddress){
            list.add(url.getUrl());
        }

        return list;
    }

    /**
     * Μετάλλαξη της λίστας διευθύνσεων
     * @param urls, νέα λίστα διευθύνσεων με αντικείμενα Url
     */
    public void setUrlAddress(List<Url> urls){
        this.urlAddress = urls;
    }
    public void addUrlAddress(Url url){
        urlAddress.add(url);
    }

    public void removeUrlAddress(Url url){
        urlAddress.remove(url);
    }

    /**
     * @return λίστα με τις ετικέτες, αντικείμενα Word, ταξινομημένο αλφαβητικά
     */
    public List<Word> getWord(){
        Collections.sort(words);
        return words;
    }

    /**
     * @return λίστα με τις ετικέτες με String, ταξινομημένο αλφαβητικά
     */
    public List<String> getStringListWord() {
        List<String> stringList = new ArrayList<>();
        for(Word w : words){
            stringList.add(w.getWord());
        }

        Collections.sort(stringList);

        return stringList;
    }

    /**
     * Μετάλλαξη της λίστας ετικετών
     * @param words νέο λεξικό με αντικείμενα Word
     */
    public void setWords(List<Word> words){
        Collections.sort(words);
        this.words = words;
    }

    /**
     * Πρόσθεση μια νέας ετικέτας (αντικείμενο Word)
     * @param word αντικείμενα Word
     */
    public void addWord(Word word){
        if(!words.contains(word)) {
            words.add(word);
            Collections.sort(words);
        }
    }

    public void removeWord(Word word){
        words.remove(word);
        Collections.sort(words);
    }

    /**
     * Ανακτά τον μοναδικό αριθμό id για μια νέα επισήμανση. Διαβάζει την τιμή από το αρχείο
     * metadata_counter.txt έτσι ώστε να διασφαλιστεί η συνέχεια των id μεταξύ διαφορετικών
     * sessions.
     * @return ο τρέχον αριθμός
     */
    private int counterInitialization(){
        if(path == null){
            System.out.println("Πρόβλημα δημιουργίας");
            return -1;
        }

        File file = new File(path);

        File parentDir = file.getParentFile();

        if(parentDir != null && !parentDir.exists()){
            if(parentDir.mkdir()){
                System.out.println("Δημιουργήθηκε ο φάκελος metadata");
            }
        }
        String line = "0";

        if(!file.exists() || file.length() == 0){
            return 0;
        }

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(path))){
            line = bufferedReader.readLine();
        }
        catch (IOException e){
            System.out.println("Σφάλμα κατά το άνοιγμα του αρχείου");
            e.printStackTrace();
        }

        try {
            if (line != null) {
                return Integer.parseInt(line);
            }
        } catch (NumberFormatException e) {
            System.out.println("Σφάλμα στη μετατροπή του ID σε αριθμό. Χρησιμοποιείται 0.");
            return -1;
        }
        return 0;
    }

    /**
     * @return ο id της επισήμανσης
     */
    public int getIdCounter(){
        return idCounter;
    }

    /**
     * Κάθε φορά που δίνεται νέος αριθμός το id αυξάνεται κατά ένα και καλείται η
     * μέθοδος saveIdCounter για την αποθήκευσή του στο αρχείο metadata_counter.txt
     */
    public void setIdCounter(){
        idCounter++;
        saveIdCounter();
    }

    /**
     * Αποθήκευση του id των επισημάνσεων. Με έναν FileWriter εγγράφεται το
     * τελευταίο κάθε φορά id στο αρχείο metadata_counter.txt
     */
    public void saveIdCounter(){
        try(FileWriter fileWriter = new FileWriter(path)){
            fileWriter.write(String.valueOf(idCounter));
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
}
