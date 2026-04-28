package com.example.application.backEnd.settings;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Η κλάση TranslationProvider υλοποιεί τη διεπαφή I18NProvider της Vaadin για
 * τη διαχείριση των μεταφράσεων της εφαρμογής.
 * Επιτρέπει δυναμική αλλαγή της γλώσσας αντλώντας τα κείμενα από το
 * Resource Bundle 'translations' ανάλογα με το επιλεγμένο Locale
 */
@Component
public class TranslationProvider implements I18NProvider {
    /**
     * Καθορίζει τις υποστηριζόμενες γλώσσες
     * @return λίστα με τα διαθέσιμα Locale
     */
    @Override
    public List<Locale> getProvidedLocales() {
        return List.of(Locale.forLanguageTag("el"), Locale.forLanguageTag("en"));
    }

    /**
     * Ανακτά τη μετάφραση για το συγκεκριμένο κλειδί και γλώσσα
     * @param s το κλειδί της μετάφρασης
     * @param locale η γλώσσα που πρέπει να επιστρέψει το κείμενο
     * @param objects προαιρετικοί παράμετροι
     * @return το μεταφρασμένο κείμενο από το Resource Bundle 'translations'
     */
    @Override
    public String getTranslation(String s, Locale locale, Object... objects) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("translations", locale);
        return resourceBundle.getString(s);
    }
}
