package com.example.application.backEnd.scraping;

import com.microsoft.playwright.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Η κλάση PlayWrightDeepScraping εκτελεί web scraping σε δυναμικές σελίδες.
 * Χρησιμοποιεί τη βιβλιοθήκη PlayWright για την απόδοσή της σε περιβάλλον Chromium
 * επιτρέποντας την εξαγωγή κειμένου και εικόνας τα οποία δεν είναι ορατά από
 * στατικό parsing
 */
public class PlaywrightDeepScraping implements AutoCloseable {

    private Browser browser;
    private Playwright playwright;

    /**
     * Κατασκευαστής κλάσης
     */
    public PlaywrightDeepScraping() {

        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().
                setHeadless(true).setSlowMo(50));
    }

    /**
     * Ανακτά το κείμενο μιας δυναμικής σελίδας. Το PlayWright ανοίγει έναν browser (chromium)
     *  και εξάγει το κείμενο. Πραγματοποιεί καθαρισμό από κενά και αλλαγή σειράς και εξάγει
     *  ένα ενιαίο string.
     *  To setHeadless ορίζεται true έτσι ώστε να μην ανοίγει νέο παράθυρο με τη σελίδα
     * @param url, η σελίδα αναζήτησης
     * @return το κείμενο της σελίδας.
     */
    public String getTextPlaywright(String url) {

        String newBody = "";

        try {
            Page page = browser.newPage();
           try {

               page.navigate(url);

               String allText = page.innerText("body");
               String[] textList = allText.replaceAll("\\s+", " ").split(" ");

               StringBuilder stringBuilder = new StringBuilder();
               for (String str : textList) {
                   if (!str.isBlank()) {
                       stringBuilder.append(str);
                       stringBuilder.append(" ");
                   }
               }

               newBody = stringBuilder.toString().trim();
           }
           finally{
               page.close();
           }
        }catch (Exception e){
            return "errorText";
        }

        return newBody;
    }

    /**
     * Εξάγει μια λίστα με εικόνες από μια δυναμική σελίδα. Η μέθοδος εκτελεί αυτόματη
     * κύλιση στο τέλος της σελίδας και αναμονή ώστε να φορτωθούν όλες οι εικόνες.
     * To setHeadless ορίζεται true έτσι ώστε να μην ανοίγει νέο παράθυρο με τη σελίδα
     * @param url, διεύθυνση για επισήμανση
     * @return  λίστα με αντικείμενα Images
     */
    public List<Images> getListImagePlaywright(String url) {

        List<Images> listImages = new ArrayList<>();

        try  {
            Page page = browser.newPage();
            try {

                page.navigate(url);

                page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
                page.waitForTimeout(2000);

                List<Locator> locators = page.locator("img").all();

                for (Locator locator : locators) {
                    String imgUrl = getImageUrl(locator, url);
                    if (!imgUrl.equals("errorImageUrl")) {
                        String caption = locator.getAttribute("alt") != null ? locator.getAttribute("alt") : "";
                        Images img = new Images(page.url(), imgUrl, caption);
                        listImages.add(img);
                    } else {
                        return null;
                    }

                }
            }finally{
                page.close();
            }

        } catch (Exception e) {
            return null;
        }

        return listImages;
    }

    /**
     * Επιστρέφει το απόλυτο url της εικόνας. H μέθοδος resolve διορθώνει και
     * επιστρέφει το σωστό url της εικόνας, μετατρέπει το σχετικό μονοπάτι σε πλήρες
     * με βάση το url της σελίδας.
     * @param locator
     * @param pageUrl διεύθυνση εικόνας
     * @return τη διεύθυνση της εικόνας
     */
    private String getImageUrl(Locator locator, String pageUrl){
        try {
            URI url = new URI(pageUrl);
            String imageUrl = url.resolve(locator.getAttribute("src")).toString();
            if(imageUrl == null || imageUrl.isBlank()){
                return "errorImageUrl";
            }
            return imageUrl;
        } catch (URISyntaxException e) {
            return "errorImageUrl";
        }

    }

    /**
     * Με τη μέθοδο close κλείνει ο browser και το playwright
     */
    @Override
    public void close(){
        if(browser != null){
            browser.close();
        }
        if(playwright != null){
            playwright.close();
        }
    }
}
