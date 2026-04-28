package com.example.application.backEnd.settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Στο enum LabelStanfordCoreNLP απεικονίζονται οι κλάσεις του μοντέλου
 * StanfordCoreNLP
 */
public enum LabelsStanfordCoreNLP {
    LOCATION, ORGANIZATION, DATE, MONEY, PERSON, PERCENT, TIME, O;

    LabelsStanfordCoreNLP(){}

    /**
     * Η μέθοδος getKeys() επιστρέφει τις κλάσεις του μοντέλου StanfordCoreNLP
     * @return
     */
    public static List<String> getKeys() {
        List<String> list = new ArrayList<>();
        for (LabelsStanfordCoreNLP l : LabelsStanfordCoreNLP.values()) {
            list.add(l.name());
        }
        return list;

    }
}
