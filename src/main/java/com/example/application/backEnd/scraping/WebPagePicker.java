package com.example.application.backEnd.scraping;

import com.example.application.backEnd.settings.Parameters;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Στην κλάση WebPagePicker επιστρέφει τα αντικείμενα WebPage
 */
public class WebPagePicker {
    private Parameters parameters;

    public WebPagePicker(Parameters parameters) {
        this.parameters = parameters;
    }

    public WebPage pickWebPage(String url){
        WebPageFetcher fetcher = new WebPageFetcher();
        WebPageParser parser = new WebPageParser();

        WebPage page;
        try {
            Document doc = fetcher.fetch(url);

            page = parser.parse(parameters, url, doc);

            return page;

        } catch (IOException e) {
          //  System.out.println("Δεν βρέθηκε κείμενο!");
            return null;
        }

    }
}
