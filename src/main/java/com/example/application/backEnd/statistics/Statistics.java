package com.example.application.backEnd.statistics;

import com.example.application.backEnd.dataHandling.*;
import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.handledFiles.HandledJsonFile;
import com.vaadin.copilot.shaded.helger.css.property.CSSPropertyEnumOrURL;

import java.util.*;

/**
 * Στην κλάση Statistics είναι υπεύθυνη για τα στατιστικά του project.
 * Επεξεργάζεται τα AnnotatedData και υπολογίζει διάφορα μεγέθη όπως συχνότητα εμφάνισης,
 * συσχετίσεις και κατανομές.
 */
public class Statistics {

    private final HandledProjects handledProjects;
    private Map<String, AnnotatedData> annotatedDataMap;

    /**
     * Κατασκευαστής της κλάσης.Γίνεται αρχικοποίηση του annotatedDataMap
     * <url, AnnotatedData>.
     * @param handledProjects, είναι @Component, @VaadinSessionScope, δημιουργείται
     *                         ένα αντίγραφο για κάθε χρήστη
     */
    public Statistics(HandledProjects handledProjects){
        this.handledProjects = handledProjects;
        this.annotatedDataMap = new HashMap<>();
        //initAnnotatedDataList();
    }

    /**
     * Αρχικοποίηση του Map<url, AnnotatedData>. Φορτώνει τα δεδομένα στη μνήμη.
     * Χρησιμοποιεί τη HandledJsonFile για την ανάγνωση του json και τη μετατροπή σε MAP
     * για την αποδοτικότερη αναζήτηση. ΟMap έχει κλειδί το url και το value είναι τα AnnotatedData
     */
    public boolean initAnnotatedDataList(){
        if(handledProjects.getProjectName() != null && !handledProjects.getProjectName().equals("")){
            HandledJsonFile jsonFile = new HandledJsonFile(handledProjects);
            jsonFile.initHandledJsonFile();
            try {
                List<AnnotatedData> list = jsonFile.loadJsonFile();

                annotatedDataMap = convertListToMap(list);

            } catch (Exception e) {

                return false;
            }

        }
        return true;
    }

    /**
     * Η μέθοδος getTotPages δίνει το σύνολο των σελίδων που έχει γίνε επισήμανση
     * @return το μέγεθος του Map<url, AnnotatedDada>
     */
    public int getTotPages() {
        return annotatedDataMap.size();
    }

    /**
     * H μέθοδος getTotAnnotations υπολογίζει το σύνολο των επισημάνσεων που έχουν
     * πραγματοποιηθεί. Δηλαδή υπολογίζει τις επισημάνσεις που έγιναν σε κείμενο, εικόνα
     * και εικόνα και κείμενο. Διατρέχει τον χάρτη annotatedDataMap και υπολογίζει για κάθε url
     * το μέγεθος κάθε λίστας επισημάνσεων (κειμένου, εικόνας, εικόνας με κείμενο).
     * @return το σύνολο των επισημάνσεων που έγιναν στο project
     */
    public int getTotAnnotations() {
        int countAnnotations = 0;

        for (Map.Entry<String, AnnotatedData> entry : annotatedDataMap.entrySet()) {
            countAnnotations += (entry.getValue().getTextList() !=null ? entry.getValue().getTextList().size() : 0);
            countAnnotations += (entry.getValue().getImageList() !=null ? entry.getValue().getImageList().size() : 0);
            countAnnotations += (entry.getValue().getImageAddTextList() !=null ? entry.getValue().getImageAddTextList().size() : 0);
        }
        return countAnnotations;
    }

    /**
     *  Η μέθοδος getAnnotationsPerPage υπολογίζει το σύνολο των επισημάνσεων κειμένου,
     *  εικόνας, εικόνας με κείμενο και συγχώνευσης επισημάνσεων που έγιναν σε κάθε σελίδα.
     *  Διατρέχει τον χάρτη annotatedDataMap και υπολογίζει το μέγεθος κάθε λίστας του AnnotatedData
     * @return Map<url, countAnnotation> το κλειδί είναι η διεύθυνση και το value είναι το άθροισμα του μεγέθους
     * κάθε λίστας του AnnotatedData για το συγκεκριμένο url
     */
    public Map<String, Integer> getAnnotationsPerPage(){
        Map<String , Integer> map = new HashMap<>();

        for (Map.Entry<String, AnnotatedData> entry : annotatedDataMap.entrySet()) {
            int countAnnotations = 0;
            countAnnotations += (entry.getValue().getTextList() !=null ? entry.getValue().getTextList().size() : 0);
            countAnnotations += (entry.getValue().getImageList() !=null ? entry.getValue().getImageList().size() : 0);
            countAnnotations += (entry.getValue().getImageAddTextList() !=null ? entry.getValue().getImageAddTextList().size() : 0);
            countAnnotations += (entry.getValue().getMergeList() !=null ? entry.getValue().getMergeList().size() : 0);

            map.put(entry.getKey(), countAnnotations);
        }
        return map;
    }

    /**
     * Η μέθοδος getMergedAnnotation υπολογίζει το σύνολο των συγχωνεύσεων επισημάνσεων
     * που έγιναν σε κάθε url. Δημιουργείται ένας χάρτης Map<url, countAnnotation>
     * όπου το κλειδί είναι η διεύθυνση και το value είναι το μέγεθος της λίστα που περιλαμβάνει
     * τις συγχωνεύσεις
     * @return Map<url, int>
     */
    public Map<String , Integer> getMergedAnnotation(){
        Map<String , Integer> map = new HashMap<>();

        for (Map.Entry<String, AnnotatedData> entry : annotatedDataMap.entrySet()) {
            int countAnnotations = (entry.getValue().getMergeList() !=null ? entry.getValue().getMergeList().size() : 0);
            map.put(entry.getKey(), countAnnotations);
        }
        return map;
    }

    /**
     * Η μέθοδος getRankLabelsAnnotations επιστρέφει το σύνολο εμφάνισης κάθε ετικέτας
     * στο project. Δημιουργείται ένας χάρτης όπου το κλειδί είναι η ετικέτα και value το
     * σύνολο εμφάνισης της ετικέτας. Για τον υπολογισμό χρησιμοποιείται η μέθοδος
     * addFromListAnnotations. Η μέθοδος χρησιμοποιείται τόσο για τον υπολογισμό
     * της συχνότητας εμφάνισης ετικέτας των μοντέλων όσο και του χρήστη
     * @param isUser, true αν θα υπολογιστεί η συχνότητα των ετικετών του χρήστη,
     *                false να πρόκειται για το μοντέλο
     * @return Map<label, rank>
     */
    public Map<String, Integer> getRankLabelsAnnotations(boolean isUser){
        Map<String, Integer> map = new HashMap<>();

        for(AnnotatedData entry : annotatedDataMap.values()){
            addFromListAnnotations(map, entry.getTextList(), isUser);
            addFromListAnnotations(map, entry.getImageList(), isUser);
            addFromListAnnotations(map, entry.getImageAddTextList(), isUser);
            addFromListAnnotations(map,entry.getMergeList(), isUser);
        }
        return map;
    }

    /**
     * Η μέθοδος getTopBottomLabel, τις συχνότερα και σπανιότερα ετικέτες του project
     * τόσο του χρήστη όσο και των μοντέλων επισήμανσης.
     * @param limit, πόσες ετικέτες θα εμφανιστούν
     * @param isUser, true να θα υπολογιστούν του χρήστη, false για τα μοντέλα επισήμανσης
     * @param isTop, true για τα συχνότερα labels, false για τα σπανιότερα
     * @return Map<Label, συχνότητα εμφάνισης>
     */

    public Map<String , Integer> getTopBottomLabel(int limit, boolean isUser, boolean isTop){
        Map<String, Integer> labels =  getRankLabelsAnnotations(isUser);

        Map<String, Integer> topLabels = new LinkedHashMap<>();

        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(labels.entrySet());

        Collections.sort(list, (i1, i2) -> i1.getValue().compareTo(i2.getValue()));
        if(isTop) {
            Collections.reverse(list);
        }

        int counter = 0;
        for(Map.Entry<String, Integer> item : list){
            topLabels.put(item.getKey(), item.getValue());
            counter++;
            if(counter == limit) {
                break;
            }
        }

        return topLabels;
    }

    /**
     * Η getAnnotationType υπολογίζει πόσες επισημάνσεις έγιναν για κάθε τύπο, κειμένου,
     * εικόνας, εικόνας με κείμενο και συγχώνευση επισημάνσεων. Για τον υπολογισμό
     * χρησιμοποιείται η μέθοδος addSumAnnotations. Δημιουργείται ένας χάρτης ο
     * οποίος έχει συγκεκριμένα τέσσερα κλειδιά, όσα και οι τύποι επισημάνσεων.
     * Με αυτά τα κλειδιά υπολογίζεται το μέγεθος κάθε λίστας επισημάνσεων του AnnotatedData
     * @return Map<τύπος επισήμανσης, int>
     */

    public Map<String , Integer> getAnnotationType(){
        Map<String , Integer> map = new HashMap<>();

        for(AnnotatedData entry : annotatedDataMap.values()){
            addSumAnnotations(map, "Text", entry.getTextList());
            addSumAnnotations(map, "Image", entry.getImageList());
            addSumAnnotations(map, "Image add Text", entry.getImageAddTextList());
            addSumAnnotations(map, "Merge", entry.getMergeList());

        }
        return map;
    }

    /**
     * Η μέθοδος addSumAnnotations χρησιμοποιείται απο τη μέθοδο getAnnotationType
     * για τα τέσσερα συγκεκριμένα κλειδιά υπολογίζει το μέγεθος της λίστα, κάθε τύπου
     * επισήμανσης, και κάνει merge στον χάρτη προσθέτοντας το μέγεθος της λίστας.
     * Αν δεν υπάρχει λίστα τότε κάνει merge το 0.
     * @param map ο χάρτης με τους τύπος επισήμανσης
     * @param key τέσσερις τύποι επισήμανσης
     * @param list, λίστα με τις επισημάνσεις. Οι λίστες κάνουν extends στο BaseAnnotation
     */
    
    private void addSumAnnotations(Map<String, Integer> map, String key, List<? extends BaseAnnotation> list){
        if(list == null){
            map.merge(key, 0 , Integer :: sum);
            return;
        }
        int count = list.size();
        map.merge(key, count, Integer ::sum);

    }

    /**
     * Η μέθοδος addFromListAnnotations χρησιμοποιείται από τη μέθοδο getRankLabelsAnnotations
     * Υπολογίζεται για κάθε label επισήμανσης τον αριθμό εμφάνισης του σε μια λίστα. Δηλαδή πόσες
     * φορές χρησιμοποιήθηκε αυτός ο όρος. Ανάλογα αν ζητείται τα labels του χρήστη ή των μοντέλων
     * ανακτάται η αντίστοιχη λίστα επισημάνσεων. Μέσα σε κάθε λίστα αναζητείται ο κάθε όρος και γίνεται
     * merge κάθε φορά που βρίσκεται ένας
     * @param map με τις κλάσεις επισήμανσης
     * @param list με τις επισημάνσεις
     * @param isUser true αν πρόκειται για τις επισημάνσεις του χρήστη, false για τα μοντέλα
     */
    private void addFromListAnnotations(Map<String, Integer> map, List< ? extends BaseAnnotation > list, boolean isUser) {

        if(list == null){
            return;
        }
        for(BaseAnnotation base : list){
            List<String> annotationsList = new ArrayList<>();
            if(isUser){
                annotationsList = base.getUserAnnotation();
            }
            else{
                annotationsList = base.getAnnotation();
            }
            for(String s : annotationsList){
                if(s != null && !s.isBlank()){
                    String cleanLabel = (s.contains(":") ? s.split(":")[0].toLowerCase().trim() : s.toLowerCase().trim());
                    map.merge(cleanLabel , 1, Integer :: sum );
                }
            }
        }
    }

    /**
     * Στη μέθοδο υπολογίζεται πόσες επισημάνσεις έγιναν ανά τύπο επισημάνσεων για κάθε url.
     * Δημιουργείται ένας Map<url, Map<τύπος επισήμανσης, μέγεθος>>. O Map<τύπος επισήμανσης, μέγεθος>
     * είναι LinkedHashMap για να έχει συγκεκριμένη σειρά στα κλειδιά (τύπους επισήμανσης).
     * Έτσι, για κάθε url δημιουργεί ένα linkedHashMap και υπολογίζει το μέγεθος της λίστας
     * για κάθε τύπο επισήμανσης
     * @return Map<url, Map<τύπος επισήμανσης, μέγεθος>>
     */
    public Map<String, Map<String, Integer>> getTypeAnnotationPerUrl(){
        Map<String, Map<String, Integer>> map = new HashMap<>();

        for(Map.Entry<String, AnnotatedData> entry : annotatedDataMap.entrySet()){

            Map<String, Integer> temp = new LinkedHashMap<>();
            temp.put("Text", (entry.getValue().getTextList() !=null ? entry.getValue().getTextList().size() : 0));
            temp.put("Image", (entry.getValue().getImageList() !=null ? entry.getValue().getImageList().size() : 0));
            temp.put("Image with Text", (entry.getValue().getImageAddTextList() !=null ? entry.getValue().getImageAddTextList().size() : 0));
            temp.put("Merge", (entry.getValue().getMergeList() !=null ? entry.getValue().getMergeList().size() : 0));

            map.put(entry.getKey(), temp);
        }
        return map;
    }

    /**
     * Η μέθοδος getLabels επιστρέφει ένα ταξινομημένο Set (treeSet) με όλες
     * τις κλάσεις επισήμανσης που έχουν χρησιμοποιηθεί. Χρησιμοποιεί τη μέθοδο
     * getRankLabelsAnnotations για να πάρει τη συχνότητα της κάθε ετικέτας
     * @param isUser, true αν πρόκειται για τις ετικέτες του χρήστη, false για τις ετικέτες των μοντέλων
     * @return ταξινομημένο Set με τις ετικέτες
     */

    public Set<String> getLabels(boolean isUser){
        Set<String> setLabels = new TreeSet<>();
        Map<String, Integer> map = getRankLabelsAnnotations(isUser);

        setLabels.addAll(map.keySet());

        return setLabels;

    }

    /**
     * Η μέθοδος getAllUrls επιστρέφει όλες τις διευθύνσεις που έχει πραγματοποιηθεί επισήμανση
     * @return Set με τις διευθύνσεις
     */
    public Set<String> getAllUrls(){
        Set<String> setUrls = new TreeSet<>();
        setUrls.addAll(annotatedDataMap.keySet());

        return setUrls;
    }

    /**
     * Η μέθοδος getTypeAnnotationPerLabel υπολογίζει για κάθε label επισήμανσης
     * το πλήθος εμφάνισης τους ανά τύπο επισήμανσης (κειμένου, εικόνας, εικόνας με κείμενο
     * και συγχώνευση επισημάνσεων). Δημιουργείται ένα Map<label, Map<τύπος επισήμανσης, αριθμός εμφάνισης>>
     * Ο Map<τύπος, αριθμός εμφάνισης> είναι LinkedHashMap για να έχουν συγκεκριμένη σειρά τα κλειδιά
     * (τύποι επισήμανσης). Στην αρχή αρχικοποίηση του LinkedHashMap και για κάθε label
     * εξετάζεται με τη μέθοδο calculatePlentyLabel αν υπάρχει η κλάση μέσα στη λίστα κάθε
     * τύπου επισήμανσης.
     * @param labels, λίστα με τις κλάσεις επισήμανσης
     * @param isUser true αν οι επισημάνσεις είναι του χρήστη, false των μοντέλων
     * @return Map<label, Map<τύπος επισήμανσης, αριθμός εμφάνισης>>
     */
    public Map<String, Map<String, Integer>> getTypeAnnotationPerLabel(List<String> labels, boolean isUser){
        Map<String, Map<String, Integer>> mainMap = new HashMap<>();
        for(String label : labels) {
            Map<String, Integer> temp = new LinkedHashMap<>();

            temp.put("Text",  0);
            temp.put("Image", 0);
            temp.put("Image with Text", 0);
            temp.put("Merge", 0);


            for (AnnotatedData entry : annotatedDataMap.values()) {

                temp.merge("Text", calculatePlentyLabel(entry.getTextList(), label, isUser) , Integer :: sum);
                temp.merge("Image", calculatePlentyLabel(entry.getImageList(), label, isUser) , Integer :: sum);
                temp.merge("Image with Text", calculatePlentyLabel(entry.getImageAddTextList(), label, isUser) , Integer :: sum);
                temp.merge("Merge", calculatePlentyLabel(entry.getMergeList(), label, isUser) , Integer :: sum);
            }
            mainMap.put(label, temp);
        }

        return mainMap;
    }

    /**
     * Η μέθοδος calculatePlentyLabel υπολογίζει το πλήθος εμφάνισης ενός label μέσα
     * σε μια λίστα επισημάνσεων.
     * @param list λίστα επισημάνσεων
     * @param value η κλάση
     * @param isUser, true αν πρόκειται για τον χρήστη, false για τα μοντέλα
     * @return το σύνολο εμφάνισης της κλάσης
     */
    private int calculatePlentyLabel(List<? extends BaseAnnotation> list , String value, boolean isUser){

        if( list == null || list.isEmpty()){
            return 0;
        }
        int count = 0;
        for(BaseAnnotation item : list){

            List<String> allAnnotations = new ArrayList<>();
            if (isUser) {
                allAnnotations = item.getUserAnnotation();
            }
            else {
                allAnnotations = item.getAnnotation();
            }
            if(allAnnotations != null && !allAnnotations.isEmpty()){
                for(String str : allAnnotations){
                    if(str != null && !str.isBlank()){
                        String cleanLabel = (str.contains(":") ? str.split(":")[0].toLowerCase().trim() : str.toLowerCase().trim());
                        if(cleanLabel.equals(value.toLowerCase().trim())){
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    /**
     * Η μέθοδος getTypeAnnotationPerLabelForUrl υπολογίζει για ένα συγκεκριμένο url
     * για κάθε κλάση τους τύπους επισήμανσης εμφάνισής της. Χρησιμοποιεί τη μέθοδο
     * calculatePlentyLabel. Δημιουργείται ένα Map<label, Map<τύπο επισήμανσης, εμφανίσεις>>.
     * Ο Map<τύπο επισήμανσης, εμφανίσεις>> είναι LinkedHashMap για να έχουν συγκεκριμένη
     * διάταξη οι τύποι επισήμανσης.
     * @param labels λίστα με τις κλάσεις επισήμανσης
     * @param url η διεύθυνση που εξετάζεται
     * @param isUser true αν πρόκειται για τον χρήστη, false για τα μοντέλα
     * @return Map<label, Map<τύπο επισήμανσης, εμφανίσεις>>
     */
    public Map<String, Map<String,Integer>> getTypeAnnotationPerLabelForUrl(List<String> labels, String url, boolean isUser){

        Map<String, Map<String, Integer>> mainMap = new HashMap<>();

        for(Map.Entry<String, AnnotatedData> entry : annotatedDataMap.entrySet()) {
            if (url.equals(entry.getKey())) {

                for (String label : labels) {
                    Map<String, Integer> temp = new LinkedHashMap<>();

                    temp.put("Text", (entry.getValue().getTextList() !=null ? calculatePlentyLabel(entry.getValue().getTextList(), label, isUser)  : 0));
                    temp.put("Image", (entry.getValue().getImageList() !=null ? calculatePlentyLabel(entry.getValue().getImageList(), label, isUser) : 0));
                    temp.put("Image with Text", (entry.getValue().getImageAddTextList() !=null ? calculatePlentyLabel(entry.getValue().getImageAddTextList(), label, isUser) : 0));
                    temp.put("Merge", (entry.getValue().getMergeList() !=null ? calculatePlentyLabel(entry.getValue().getMergeList(), label, isUser) : 0));

                    mainMap.put(label, temp);
                }

            }
        }
        return mainMap;
    }

    /**
     * Η μέθοδος getTypeAnnotationPerUrlForLabel υπολογίζει για μια συγκεκριμένη ετικέτα
     * για κάθε url τους τύπος επισήμανσης εμφάνισης της. Χρησιμοποιεί τη μέθοδο
     * calculatePlentyLabel. Δημιουργείται ένα Map<url, Map<τύπο επισήμανσης, εμφανίσεις>>.
     * Ο Map<τύπο επισήμανσης, εμφανίσεις>> είναι LinkedHashMap για να έχουν συγκεκριμένη
     *  διάταξη οι τύποι επισήμανσης.
     * @param urls λίστα διευθύνσεων
     * @param label η εξεταζόμενη κλάση
     * @param isUser true αν πρόκειται για τον χρήστη, false για τοα μοντέλα
     * @return Map<url, Map<τύπο επισήμανσης, εμφανίσεις>>
     */
    public Map<String, Map<String,Integer>> getTypeAnnotationPerUrlForLabel(List<String> urls, String label, boolean isUser){

        Map<String, Map<String, Integer>> mainMap = new HashMap<>();

        for(String url : urls) {
            Map<String, Integer> temp = new LinkedHashMap<>();

            AnnotatedData entry = annotatedDataMap.get(url);
            temp.put("Text", (entry.getTextList() !=null ? calculatePlentyLabel(entry.getTextList(), label, isUser)  : 0));
            temp.put("Image", (entry.getImageList() !=null ? calculatePlentyLabel(entry.getImageList(), label, isUser) : 0));
            temp.put("Image with Text", (entry.getImageAddTextList() !=null ? calculatePlentyLabel(entry.getImageAddTextList(), label, isUser) : 0));
            temp.put("Merge", (entry.getMergeList() !=null ? calculatePlentyLabel(entry.getMergeList(), label, isUser) : 0));

            mainMap.put(url, temp);

            }

        return mainMap;
    }

    /**
     * Η μέθοδος correlationMatrixLabels δημιουργεί ένα πίνακα συσχέτισης (heatMap)
     * όπου εμφανίζονται οι ετικέτες του χρήστη ή των μοντέλων. Υπολογίζεται σε πόσα url
     * έχουμε εμφανίζονται ταυτόχρονα στς επισημάνσεις δύο κλάσεις
     * @param labels λίστα με τις ετικέτες επισήμανσης
     * @param isUser true αν πρόκειται για τον χρήστη, false για τα μοντέλα
     * @return Map<label, Map<label, συχνότητα εμφάνισης>>
     */
    public Map<String, Map<String, Integer>> correlationMatrixLabels(List<String> labels, boolean isUser){
        Map<String, Map<String, Integer>> matrix = new LinkedHashMap<>();

        for(String label : labels){
            matrix.put(label , new LinkedHashMap<>());
            for(String collabLabels : labels){
                matrix.get(label).put(collabLabels, 0);
            }
        }

        for(AnnotatedData entry : annotatedDataMap.values()){
            Set<String> entryLabels = new HashSet<>();
            entryLabels.addAll(getLabelsPerUrl(entry.getTextList(), isUser));
            entryLabels.addAll(getLabelsPerUrl(entry.getImageList(), isUser));
            entryLabels.addAll(getLabelsPerUrl(entry.getImageAddTextList(), isUser));
            entryLabels.addAll(getLabelsPerUrl(entry.getMergeList(), isUser));

            List<String> listLabels = entryLabels.stream()
                    .map(s -> s.contains(":") ? s.split(":")[0].toLowerCase().trim() : s.toLowerCase().trim())
                    .filter(labels::contains).distinct().toList();

            for(String label1: listLabels){
                for(String label2 : listLabels){
                    matrix.get(label1).merge(label2 , 1, Integer ::sum);
                }
            }
        }
        return matrix;

    }

    /**
     * Η μέθοδος getLabelsPerUrl επιστρέφει τις κλάσεις επισήμανσης για κάθε url
     * @param list λίστα με BaseAnnotation, λίστα επισημάνσεων
     * @param isUser true αν πρόκειται για τον χρήστη, false για τα μοντέλα
     * @return set με τα label επισήμανσης
     */
    private Set<String> getLabelsPerUrl(List<? extends BaseAnnotation> list, boolean isUser){
        if(list == null || list.isEmpty()){
            return new HashSet<>();
        }

        Set<String> labels = new HashSet<>();
        for(BaseAnnotation base : list){
            if(isUser) {
                labels.addAll(base.getUserAnnotation());
            }else{
                labels.addAll(base.getAnnotation());
            }
        }
        return labels;
    }

    /**
     * Η μέθοδος correlationModelLabelVsUser δημιουργεί ένα χάρτη συσχέτισης μεταξύ των
     * επισημάνσεων από τα μοντέλα με τις επισημάνσεις του χρήστη.
     * @param labels
     * @param userLabels
     * @return
     */
    public Map<String, Map<String, Integer>> correlationModelLabelVsUser(List<String> labels, List<String> userLabels){
        Map<String, Map<String, Integer>> matrix = new LinkedHashMap<>();

        for(String label : labels){
            matrix.put(label , new LinkedHashMap<>());
            for(String collabLabels : userLabels){
                matrix.get(label).put(collabLabels, 0);
            }
        }

        for(AnnotatedData entry : annotatedDataMap.values()){
            Set<String> entryLabels = new HashSet<>();
            entryLabels.addAll(getLabelsPerUrl(entry.getTextList(), false));
            entryLabels.addAll(getLabelsPerUrl(entry.getImageList(), false));
            entryLabels.addAll(getLabelsPerUrl(entry.getImageAddTextList(), false));
            entryLabels.addAll(getLabelsPerUrl(entry.getMergeList(), false));

            List<String> listLabels = entryLabels.stream()
                    .map(s -> s.contains(":") ? s.split(":")[0].toLowerCase().trim() : s.toLowerCase().trim())
                    .filter(labels::contains).distinct().toList();

            Set<String> entryUserLabels = new HashSet<>();
            entryUserLabels.addAll(getLabelsPerUrl(entry.getTextList(), true));
            entryUserLabels.addAll(getLabelsPerUrl(entry.getImageList(), true));
            entryUserLabels.addAll(getLabelsPerUrl(entry.getImageAddTextList(), true));
            entryUserLabels.addAll(getLabelsPerUrl(entry.getMergeList(), true));

            List<String> listUserLabels = entryUserLabels.stream()
                    .map(s -> s.contains(":") ? s.split(":")[0].toLowerCase().trim() : s.toLowerCase().trim())
                    .filter(userLabels::contains).distinct().toList();

            for(String label1: listLabels) {
                for (String label2 : listUserLabels) {
                    matrix.get(label1).merge(label2, 1, Integer::sum);

                }
            }
        }
        return matrix;
    }

    /**
     * Στη μέθοδο covertListToMap μετατρέπεται μια λίστα με AnnotatedData σε έναν
     * χάρτη με κλειδί το url και value το AnnotatedData
     * @param list λίστα με AnnotatedData
     * @return χάρτης Map<url, AnnotatedData>
     */
    private Map<String, AnnotatedData> convertListToMap(List<AnnotatedData> list){
        Map<String , AnnotatedData> map = new HashMap<>();

        for(AnnotatedData base: list){

            String pageUrl = base.getPageUrl();

            AnnotatedData data = map.computeIfAbsent(pageUrl, k->{
                AnnotatedData d = new AnnotatedData();
                d.setPageUrl(pageUrl);
                return d;
            });

            if(base.getTextList() != null && !base.getTextList().isEmpty()){
                data.addTextList(base.getTextList());
            }
            if(base.getImageList() != null && !base.getImageList().isEmpty()){
                data.addImageList(base.getImageList());
            }
            if(base.getMergeList() != null && !base.getMergeList().isEmpty()){
                data.addTextAndImageList(base.getMergeList());
            }
            if(base.getImageAddTextList() != null && !base.getImageAddTextList().isEmpty()){
                data.addImageAddTextList(base.getImageAddTextList());
            }

        }
        return map;
    }
}
