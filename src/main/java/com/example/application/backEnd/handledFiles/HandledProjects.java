package com.example.application.backEnd.handledFiles;

import com.example.application.backEnd.dataHandling.AnnotatedData;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Η κλάση HandedProject διαχειρίζεται όλα τα project που δημιουργεί ο χρήστης.
 * 1) Ο χρήστης έχει τη δυνατότητα να δημιουργήσει ένα νέο project. Δημιουργούνται όλοι οι φάκελοι και τα αρχεία που απαιτούνται.
 * Δημιουργείται ένας φάκελος με το όνομα του project, ο οποίος περιέχει το αρχείο projectName.json (έχουν αποθηκευτεί όλες
 * οι επισημάνσεις) έναν φάκελο images, στον οποίο θα αποθηκευτούν όλες οι εικόνες στις οποίες έχει γίνει Ψ,
 * και ένας φάκελος metadata ο οποίος περιέχει τα παρακάτω αρχεία txt. α) creationDate.txt, αποθηκεύεται η ημερομηνία δημιουργίας
 * του project, β) directory.txt το λεξικό που θα χρησιμοποιηθεί στο project, γ) metadata_counter.txt σε αυτό το αρχείο κρατάμε τον
 * τελευταίο αριθμό αντικειμένου επισήμανσης και δ) urls.txt είναι οι διευθύνσεις στις οποίες θα γίνει εξαγωγή δεδομένων
 * 2) Ο χρήστης μπορεί να ανακτήσει ένα project το οποίο είχε δημιουργήσει και να συνεχίσει τις επισημάνσεις.
 * Η κλάση είναι VaadinSessionScope δηλαδή δημιουργείται ένα αντικείμενο για κάθε session χρήστη.
 */
@Component
@Scope(value = "vaadin-session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HandledProjects implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String projectName;
    private String pathJson;
    private String pathImages;
    private String pathMetadata;
    private final String mainDir = "data/";
    private List<File> filesList = new ArrayList<>();

    /**
     * Κατασκευαστής κλάσης
     */
    public HandledProjects() {
    }

    /**
     * Κατασκευαστής κλάσης στην περίπτωση δημιουργίας ενός νέου project
     * Δημιουργούνται οι φάκελοι που χρειάζονται για τη διαχείριση του project
     * @param projectName, όνομα project.
     */
    public HandledProjects(String projectName){
        this.projectName = projectName;
        this.pathJson = mainDir + projectName + "/" + projectName + ".json";
        this.pathImages = mainDir + projectName + "/images/";
        this.pathMetadata = mainDir + projectName + "/metadata/";

        new File(this.pathImages).mkdir();
        new File(this.pathMetadata).mkdir();
    }

    /**
     * Κατασκευαστής κλάσης, αυτός ο κατασκευαστής φορτώνει τα μονοπάτια όλων
     * των φακέλων και του αρχείου json του project
     * @param projectName, όνομα project
     * @param pathJson, το μονοπάτι του json αρχείου
     * @param pathImages, το μονοπάτι του φακέλου των εικόνων
     * @param pathMetadata, το μονοπάτι των metadata
     */
    public HandledProjects(String projectName, String pathJson, String pathImages, String pathMetadata) {
        this.projectName = projectName;
        this.pathJson = pathJson;
        this.pathImages = pathImages;
        this.pathMetadata = pathMetadata;
    }

    /**
     * Οι παρακάτω μέθοδοι είναι οι getters και setters όλων των μεταβλητών της κλάσης
     * @return
     */
    public String getPathJson() {
        return pathJson;
    }

    public void setPathJson(String pathJson) {
        this.pathJson = pathJson;
    }

    public String getPathImages() {
        return pathImages;
    }

    public void setPathImages(String pathImages) {
        this.pathImages = pathImages;
    }

    public String getPathMetadata() {
        return pathMetadata;
    }

    public void setPathMetadata(String pathMetadata) {
        this.pathMetadata = pathMetadata;
    }

    public String getProjectName() {
        if(projectName == null){
            return "";
        }
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getMainDir() {
        return mainDir;
    }

    public List<File> getFilesList() {
        return filesList;
    }

    public void setFilesList(List<File> filesList) {
        this.filesList = filesList;
    }

    /**
     * Η μέθοδος "φορτώνει" το project. Από το όνομα του project, το οποίο δεν πρέπει να είναι κενό,
     * ορίζονται οι μεταβλητές projectName, pathJson, pathImage, pathMetadata.
     * Όλες οι μεταβλητές αρχίζουν από το mainDin, η οποία είναι final και έχει τιμή data/
     * @param projectName, το όνομα του αρχείου
     */
    public void loadProject(String projectName){
        if(!projectName.trim().equals("")) {
            this.projectName = projectName;
            this.pathJson = mainDir + projectName + "/" + projectName + ".json";
            this.pathImages = mainDir + projectName + "/images/";
            this.pathMetadata = mainDir + projectName + "/metadata/";

            new File(this.pathImages).mkdir();
            new File(this.pathMetadata).mkdir();

        }

    }

    /**
     * Η μέθοδος δημιουργεί ένα project. Έχει ως μεταβλητές το όνομα του αρχείου και την ημερομηνία δημιουργίας
     * Αν δεν υπάρχει το project (φάκελος με το όνομα του project) τότε δημιουργείται. Μετά καλείται η μέθοδος
     * loadProject για να ορίσει τα μονοπάτια και η μέθοδος saveDateCreatedFile στην οποία αποθηκεύεται σε αρχείο txt
     * η ημερομηνία δημιουργίας. Δημιουργείται νεο αρχείο με μονοπάτι pathJson
     * @param projectName, όνομα αρχείου
     * @param creationDate, ημερομηνία δημιουργίας
     * @return Επιστρέφει true μετά την επιτυχή δημιουργία του αρχείου, διαφορετικά false
     */
    public boolean createProject(String projectName, LocalDate creationDate){

        File root = new File(mainDir + projectName);
        if(!root.exists()){
            root.mkdir();
        }

        loadProject(projectName);
        try {
            saveDateCreatedFile(creationDate);
        } catch (Exception e) {
            return false;
        }
        File project = new File(pathJson);
        try {
            project.createNewFile();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Η μέθοδος δίνει μια λίστα με όλα τα αρχεία που υπάρχουν στο mainDir (φάκελος data)
     * Στην αρχή αδειάζει η λίστα. Αν το mainDir δεν υπάρχει ή δεν είναι φάκελος επιστρέφεται μια νέα λίστα.
     * Αν η λίστα των αρχείων που υπάρχει μέσα στο mainDir είναι null ή είναι άδεια τότε πάλι επιστρέφει μια νέα λίστα.
     * Διαφορετικά εκτελεί την αναδρομική μέθοδο recursiveFindFiles για να βρει όλα τα αρχεία που υπάρχουν στους υποφακέλους
     * του mainDir.
     * @return μια λίστα με τα αρχεία που υπάρχουν μέσα στο mainDir
     */

    public List<File> getAllProjectNames(){
        this.filesList.clear();

        File root = new File(mainDir);
        if(!root.exists() || !root.isDirectory()){
            root.mkdirs();
            return new ArrayList<>();
        }

        File[] list =  root.listFiles();
        if(list == null || list.length == 0){
            return new ArrayList<>();
        }

        for(File file : list){
            recursiveFindFiles(file);
        }
        return this.filesList;
    }

    /**
     * Αναδρομική μέθοδος, για κάθε File εξετάζεται αν είναι αρχείο ή φάκελος. Στην περίπτωση αρχείου
     * και αν τελειώνει σε json (στην ουσία υπάρχει το project μέσα στον φάκελο) τότε προστίθεται το όνομα
     * του αρχείου μέσα στη λίστα των αρχείων. Αν είναι φάκελος, δημιουργείται λίστα με όλους τους υποφακέλους
     * που υπάρχουν μέσα στον φάκελο και εκτελείται αναδρομικά για κάθε έναν φάκελο η recursiveFindFiles
     * @param file το αρχείο ή φάκελος που θα εξεταστεί να τελειώνει σε json
     */

    private void recursiveFindFiles(File file){

            if(file.isFile()){
                String fileName = file.getName();
                if(fileName.endsWith("json")){
                    filesList.add(file);
                }
            }
            else if(file.isDirectory()){
                File[] files = file.listFiles();
                if(file != null) {
                    for (File newFile : files) {
                        recursiveFindFiles(newFile);
                    }
                }
            }
    }

    /**
     * Υπολογίζεται αναδρομικά το μέγεθος όλου του project. Καλείται η αναδρομική σχέση
     * recursiveFilesSize η οποία στον φάκελο του project εξετάζει όλα τα αρχείο
     * @param project, όνομα project
     * @return το μέγεθος του project σε KB
     */

    public Long getSizeProject_KB(File project){

        File root = new File(project.getParentFile().toURI());
        if(!root.exists() || !root.isDirectory()){
            return 0L;
        }

        long size = recursiveFilesSize(root);

        return size / 1024;
    }

    /**
     * Αναδρομική σχέση, στην οποία μέσα στον φάκελο του project εκτελεί την αναδρομή
     * μέχρι να φτάσει σε όλα τα αρχεία και από τα οποία παίρνει το μέγεθος του καθενός από αυτά.
     * @param file, αρχείο ή φάκελος που θα εξεταστεί
     * @return στο τέλος της αναδρομής, έχει εξερευνήσει όλο τον φάκελο του project και έχει πάρει
     * το μέγεθος κάθε αρχείου, επιστρέφει το συνολικό μέγεθος του project
     */
    private Long recursiveFilesSize(File file){
        long size = 0L;

        if(file.isFile()){
            return file.length();
            }

        else if(file.isDirectory()){
            File[] files = file.listFiles();
            if(file != null) {
                for (File newFile : files) {
                    size += recursiveFilesSize(newFile);
                }
            }
        }
        return size;
    }

    /**
     * Αποθήκευση της ημερομηνίας δημιουργίας του project στο αρχείο creationDate.txt.
     * Η ημερομηνία έχει συγκεκριμένη μορφή dd/MM/yyyy και μέσω του BufferWriter γράφεται στο αρχείο
     * @param localDateCreated ημερομηνία δημιουργίας
     */
    private boolean saveDateCreatedFile(LocalDate localDateCreated){
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String formattedDate = localDateCreated.format(myFormatObj);
        String path = pathMetadata + "creationDate.txt";
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path))){
            bufferedWriter.write(formattedDate);
            return true;
        }
        catch (IOException e){
            //e.printStackTrace();
            return false;
        }

    }

    /**
     * Ανάκτηση της ημερομηνίας δημιουργίας, από το αρχείο δημιουργείται το μονοπάτι
     * μέχρι το creationDate.txt και διαβάζει απο το αρχείο την ημερομηνία
     * @param project, το project
     * @return την ημερομηνία δημιουργίας του αρχείου.
     */
    public LocalDate loadCreationDate(File project){

        Path path = Paths.get( project.getParent(),  "metadata", "creationDate.txt");
        LocalDate creationDate = null;

        try{
            String date = Files.readString(path).trim();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                creationDate = LocalDate.parse(date, formatter);

        }catch (IOException e){
           // e.printStackTrace();
            return null;
        }

        return creationDate;
    }

    /**
     * Διαγραφή project, διαγράφονται όλα τα αρχεία και οι φάκελοι που υπάρχουν μέσα
     * project. Αν το project είναι φάκελος τότε σε πίνακα μαζεύονται όλα τα αρχεία
     * και εκτελείται αναδρομικά η deleteProject και επιστρέφει true αν διαγραφεί
     * το κάθε αρχείο.
     * @param project φάκελος ή αρχείο μέσα στο project
     * @return true αν διαγραφεί ένα αρχείο και false διαφορετικά
     */
    public boolean deleteProject(File project){

        if(project == null || !project.exists()){
            return false;
        }
        if(project.isDirectory()){
            File[] files = project.listFiles();

            if(files != null){
                for(File file : files){
                    deleteProject(file);
                }
            }

        }
        if(project.delete()){
            return true;
        }
        return false;
    }

    /**
     * Γίνεται αρχικοποίηση όλων των μονοπατιών
     */
    public void clearProject(){
        this.projectName = "";
        this.pathJson = "";
        this.pathImages = "";
        this.pathMetadata = "";
        this.filesList.clear();
    }
}
