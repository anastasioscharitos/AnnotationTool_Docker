package com.example.application.backEnd.scraping;

import com.example.application.backEnd.settings.Parameters;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * Η κλάση WebPageParser μετασχηματίζει το αντικείμενο Document σε αντικείμενο
 * WerPage. Εξάγει τον τίτλο, το κείμενο και τις εικόνες μέσω του ImagePicker
 */
public class WebPageParser {
    private Parameters parameters;

    /**
     * Αναλύει το έγγραφο Document και δημιουργεί ένα αντικείμενο WebPage.
     * @param parameters παράμετροι ρύθμισης
     * @param url η διεύθυνση της σελίδας
     * @param document το HTML που έχει ανακτηθεί, αντικείμενο Document
     * @return αντικείμενα WebPage
     */
    public WebPage parse(Parameters parameters, String  url, Document document) {
        try {
            this.parameters = parameters;
            String title = document.title();

            String webPageText = pageText(document);

            ImagePicker imagePicker = new ImagePicker(parameters, url, document);

            List<Images> images = imagePicker.getImages();

            return new WebPage(url, title, webPageText, images);
        }
        catch (ExceptionInInitializerError e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Στη μέθοδο PageText εξάγει το κείμενο που υπάρχει μέσα στη σελίδα.
     * Δίνει προτεραιότητα σε tags article, main και αφαιρεί τα header, footer, nav,
     * aside, script, style, lang. Έτσι, διατηρεί το κείμενο σε παραγράφους, κεφαλίδες και λίστες.
     * (p, h1, h2, h3, h4, h5, h6, blockquote, li).
     * @param document το HTML που έχει ανακτηθεί, αντικείμενο Document
     * @return ένα ενιαίο String με το κείμενο που υπάρχει στη σελίδα
     */
    private String pageText(Document document) {
        Element mainContent = document.selectFirst("article, main");

        if (mainContent == null) {
            mainContent = document.body();
        }

        mainContent.select("header, footer, nav, aside, script, style, lang").remove();

        Elements paragraphs = mainContent.select("p, h1, h2, h3, h4, h5, h6, blockquote, li ");
        StringBuilder stringBuilder = new StringBuilder();
        for (Element paragraph : paragraphs) {
            String str = paragraph.text().trim();

            stringBuilder.append(str);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }


}
