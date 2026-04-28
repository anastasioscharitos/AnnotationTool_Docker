package com.example.application.backEnd.settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Στο enum LabelSpacy απεικονίζονται οι κλάσεις του μοντέλου spaCy
 */
public enum LabelsSpaCy {
    CARDINAL("Numerals that do not fall under another type"),
    DATE("Absolute or relative dates or periods"),
    EVENT("Named hurricanes, battles, wars, sports events, etc."),
    FAC("Buildings, airports, highways, bridges, etc."),
    GPE("Countries, cities, states"),
    LANGUAGE("Any named language"),
    LAW("Named documents made into laws."),
    LOC("Non-GPE locations, mountain ranges, bodies of water"),
    MONEY("Monetary values, including unit"),
    NORP("Nationalities or religious or political groups"),
    ORDINAL("\"first\", \"second\", etc."),
    ORG("Companies, agencies, institutions, etc."),
    PERCENT("Percentage, including \"%\""),
    PERSON("People, including fictional"),
    PRODUCT("Objects, vehicles, foods, etc. (not services)"),
    QUANTITY("Measurements, as of weight or distance"),
    TIME("Times smaller than a day"),
    WORK_OF_ART("Titles of books, songs, etc.");

    private final String label;

    LabelsSpaCy(String label) {
        this.label = label;
    }

    /**
     * Η μέθοδος getLabels επιστρέφει όλα τα labels του μοντέλου spacy
     * @return λίστα με τα label του μοντέλου
     */
    public static List<String> getLabels() {
        List<String> list = new ArrayList<>();
        for (LabelsSpaCy l : LabelsSpaCy.values()) {
            list.add(l.label);
        }
        return list;
    }

    /**
     * Η μέθοδος getKeys() επιστρέφει τις κλάσεις του μοντέλου spaCy
     * @return, λίστα με τις κλάσεις των μοντέλων
     */
    public static List<String> getKeys() {
        List<String> list = new ArrayList<>();
        for (LabelsSpaCy l : LabelsSpaCy.values()) {
            list.add(l.name());
        }
        return list;
    }

    /**
     * Η μέθοδος getTags επιστρέφει μια λίστα με String που αποτελείται από την κλάση
     * και την επεξήγηση της
     * @return
     */
    public static List<String> getTags(){
        List<String> list = new ArrayList<>();
        for (LabelsSpaCy l : LabelsSpaCy.values()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(l.name());
            stringBuilder.append(" : ");
            stringBuilder.append(l.label);
            list.add(stringBuilder.toString());
        }
        return list;

    }
}

