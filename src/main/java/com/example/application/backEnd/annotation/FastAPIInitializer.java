package com.example.application.backEnd.annotation;


import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Η κλάση FastAPIInitializer αρχικοποιεί τον FastAPI server. Κατά την εκκίνηση της εφαρμογής
 * γίνεται ενεργοποίηση του FastAPI server. Χρησιμοποιεί την κλάση FastAPIManager
 * για να διαπιστώσει ότι ο server είναι σε λειτουργία, διαφορετικά δρομολογεί την εκκίνηση του
 * και αναμένει επιβεβαίωση. Έτσι, διασφαλίζει ότι τα μοντέλα επισήμανσης (spaCy, YOLO,
 * YOLO World και CLIPS) είναι διαθέσιμα πριν από την πρώτη χρήση.
 */
@Service
public class FastAPIInitializer {
    private FastAPIManager fastAPIManager;

    public FastAPIInitializer(FastAPIManager fastAPIManager) {
        this.fastAPIManager = fastAPIManager;
    }

    /**
     * Η μέθοδος init εκτελείται αυτόματα όταν η εφαρμογή είναι έτοιμη. Ελέγχει την κατάσταση του
     * server και αν χρειαστεί εκτελεί επανεκκίνησή του.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        System.out.println("Checking FastAPI connection at: " + fastAPIManager.getFastApiUrl());
        if (!fastAPIManager.isServerAlive()) {
            System.out.println("FastAPI is not ready yeet");
//            Map<String, String> status = fastAPIManager.startServer();
//            String txt = fastAPIManager.convertModel(status);
//            System.out.println(txt);
        }
    }
}