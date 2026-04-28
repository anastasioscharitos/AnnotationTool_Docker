package com.example.application.backEnd.handledFiles;

import com.example.application.backEnd.uploadData.Url;
import com.example.application.backEnd.uploadData.Word;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Η κλάση UploadSaveData διαχειρίζεται το λεξικό που θα χρησιμοποιηθεί για την
 * επισήμανση εικόνων για τα μοντέλα, CLIPS και YOLO World, και τη λίστα των
 * διευθύνσεων. Γίνεται η "φόρτωση" και αποθήκευση της λίστα ετικετών και των διευθύνσεων.
 */
public class UploadSaveData {

    private final HandledProjects handledProjects;

    private String pathUrls ;
    private String pathWords;

    /**
     * Κατασκευαστής της κλάσης, έχει παράμετρο HandledProject για να μπορεί να
     * έχει πρόσβαση στο φάκελο metadata, όπου αποθηκεύονται οι λίστες ετικετών και
     * διευθύνσεων.
     * @param handledProjects
     */
    public UploadSaveData(HandledProjects handledProjects){
        this.handledProjects = handledProjects;
    }

    /**
     * Αρχικοποίηση των μονοπατιών της λίστας των ετικετών και των διευθύνσεων
     */
    public void initUploadSaveData(){
        this.pathUrls = handledProjects.getPathMetadata() + "urls.txt";
        this.pathWords = handledProjects.getPathMetadata() + "dictionary.txt";
    }

    /**
     * Η μέθοδος μετατρέπει έναν πίνακα bytes σε λίστα συμβολοσειρών. Χρησιμοποιείται
     * για την επεξεργασία δεδομένων που εισάγονται δυναμικά στην εφαρμογή
     * @param bytes, πίνακας bytes των αρχείων,
     * @return λίστα σπό String (urls και λεξικό)
     */
    public List<String> loadUrlsWords(byte[] bytes){
        List<String> list = new ArrayList<>();

        if(bytes == null || bytes.length == 0){
            return list;
        }

        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(bytes)))){

            String line;
            while((line = bufferedReader.readLine()) != null){
                list.add(line);

            }

        } catch (IOException e) {
            new ArrayList<>();
        }
        return list;
    }

    /**
     * Φόρτωση της λίστα διευθύνσεων από τοπικό αρχείο urls.txt. Με τον BufferReader
     * διαβάζεται το αρχείο με τις διευθύνσεις
     * @return λίστα με τις διευθύνσεις
     */
    public List<Url> loadUrls(){

        List<Url> urlsList = new ArrayList<>();

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(pathUrls))){
            String line;
            while((line = bufferedReader.readLine()) != null){

                Url url = new Url(line);
                urlsList.add(url);

            }
        }catch (FileNotFoundException e){
            return  new ArrayList<>();
        }catch (IOException e){
            return new ArrayList<>();
        }
        return urlsList;

    }

    /**
     * Φόρτωση της λίστας ετικετών από το τοπικό αρχείο directory.txt. Με τον BufferReader
     * διαβάζεται το αρχείο των ετικετών
     * @return λίστα με τις ετικέτες
     */
    public List<Word> loadWord(){


        List<Word> wordsList = new ArrayList<>();

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(pathWords))){
            String line;
            while((line = bufferedReader.readLine()) != null){

                Word word = new Word(line);
                wordsList.add(word);
            }
        }catch (FileNotFoundException e){
            return  new ArrayList<>();
        }catch (IOException e){
            return new ArrayList<>();
        }
        return wordsList;
    }

    /**
     * Αποθήκευση λίστας με τα url. Η αποθήκευση γίνεται με τον BufferWriter
     * @param urlsList, λίστα με urls που θα αποθηκευτούν
     * @return true αν ολοκληρώθηκε η αποθήκευση, false διαφορετικά. Επίσης,
     * αν το όνομα του έργου δεν υπάρχει ή είναι κενό τότε επιστρέφει false
     */
    public boolean saveUrls(List<Url> urlsList) {
        if(handledProjects.getProjectName() == null || handledProjects.getProjectName().isBlank()){
            return false;
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathUrls))) {
            for (Url url : urlsList) {
                bufferedWriter.write(url.getUrl());
                bufferedWriter.newLine();

            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Αποθήκευση λίστα ετικετών
     * Τα δεδομένα ταξινομούνται και μετά γίνεται αποθήκευση με τον BufferWriter.
     * @param wordsList, λίστα με τις ετικέτες που θα αποθηκευτούν
     * @return true αν ολοκληρώθηκε η αποθήκευση, false διαφορετικά. Επίσης,
     *  αν το όνομα του έργου δεν υπάρχει ή είναι κενό τότε επιστρέφει false
     */
    public boolean saveWords(List<Word> wordsList) {
        if(handledProjects.getProjectName() == null || handledProjects.getProjectName().isBlank()){
            return false;
        }
        List<Word> sortList = new ArrayList<>(wordsList);
        Collections.sort(sortList);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathWords))) {
            for (Word word : sortList) {
                bufferedWriter.write(word.getWord());
                bufferedWriter.newLine();

            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
