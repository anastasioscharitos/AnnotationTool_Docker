package com.example.application.backEnd.handledFiles;


import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 *  Εκτελείται κατά τη σύνδεση του Component στο UI.
 *  Όταν φορτώνεται ένα έργο (κουμπί load από το HomePage) χρησιμοποιείται το
 *  Session Attribute "LOCKED_PROJECT" για να κρατήσουμε το όνομα του έργου
 *  και να το χρησιμοποιήσουμε όταν λήξη το session. Όταν λήξει το session o SessionDestroyListener
 *  αναλαμβάνει να ξεκλειδώσει το έργο. Ένα session θα λήξει μετά από 30 λεπτά αδράνειας.
 */
@Component
public class SessionInitListener implements VaadinServiceInitListener {
    private final HandledLockProject handledLockProject;

    /**
     * Κατασκευαστής κλάσης
     * @param handledLockProject αντικείμενο HandledLockProject
      */
    public SessionInitListener(HandledLockProject handledLockProject){
        this.handledLockProject = handledLockProject;
    }

    /**
     * Ο SessionDestroyListener μόλις ξεπεραστεί ο χρόνος των 30min ξεκλειδώνει
     * το έργο. Ρυθμίζονται οι παράμετροι της υπηρεσίας Vaadin κατά την καταστροφή
     * κάθε συνεδρίας
     * @param serviceInitEvent Το γεγονός αρχικοποίησης
     */
    @Override
    public void serviceInit(ServiceInitEvent serviceInitEvent) {
        serviceInitEvent.getSource().addSessionDestroyListener(sessionDestroyEvent -> {
            try {
                String projectName = (String) sessionDestroyEvent.getSession().getAttribute("LOCKED_PROJECT");
                if (projectName != null && !projectName.isBlank()) {
                    boolean result = handledLockProject.unlockProject(projectName);
                    if (result) {
                        System.out.println("Timeout : το project " + projectName + " ξεκλειδώθηκε!");
                    }
                    else {
                        System.out.println("Σφάλμα στο ξεκλείδωμα : " + projectName);
                    }
                }
            } catch (Exception e) {
                System.err.println("Δεν μπορεί να ξεκλειδωθεί το project: " + e.getMessage());
            }
        });
    }
}
