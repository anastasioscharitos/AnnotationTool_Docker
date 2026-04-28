package com.example.application.backEnd.handledFiles;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Σκοπός της κλάσης είναι να αποτρέπει την ταυτόχρονη χρήση ενός project σε πολλά
 * άτομα. Το κάθε project θα το διαχειρίζεται ένας χρήστης για όση διαρκεί το session.
 */

@Service
public class HandledLockProject {

    private Map<String, Boolean> lockedProject = new HashMap<>();

    /**
     * Κατασκευαστής κλάσης
     */
    public HandledLockProject(){
    }

    /**
     * Προσπάθεια κλειδώματος ενός project
     * @param projectName όνομα project
     * @return true αν δεσμεύτηκε το project, false αν το project χρησιμοποιείται από άλλον
     */
    public boolean lockProject(String projectName){
        if(!lockedProject.containsKey(projectName)){
            lockedProject.putIfAbsent(projectName, true);
            return lockedProject.get(projectName);
        }
        return false;
    }

    /**
     * Ξεκλείδωμα project
     * @param projectName όνομα project
     * @return true αν το project έχει αποδεσμευτεί, διαφορετικά false
     */
    public boolean unlockProject(String projectName){
        if(lockedProject.containsKey(projectName)){
            lockedProject.remove(projectName);
            return  true;
        }
        return false;
    }

    /**
     * Έλεγχος αν το project χρησιμοποιείται από άλλον χρήστη
     * @param projectName όνομα project
     * @return true αν χρησιμοποιείται, διαφορετικά false
     */
    public boolean isLockedProject(String projectName){
        if(lockedProject.containsKey(projectName)){
            return true;
        }
        return false;
    }

    public Map<String,Boolean> getMap(){
        return lockedProject;
    }

}
