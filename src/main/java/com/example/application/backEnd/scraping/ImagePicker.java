package com.example.application.backEnd.scraping;

import com.example.application.backEnd.settings.Parameters;
import org.glassfish.json.JsonUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Κλάση αναζητά και εξάγει εικόνες από τις σελίδες.
 * Αναζητά εικόνες σε standard tags ("src", "data-src","data-lazy", "data-original",
 * "srcset", "data-srcset"), σε CSS styles (background image) και σε δεδομένα μορφής
 * json (Script tags).
 * Επίσης, πιάνει και τη λεζάντα της εικόνας
*/
public class ImagePicker {

    private List<Images> imageList;
    private Document document;
    private String url;

    private Parameters parameters;

    /**
     * Κατασκευαστής κλάσης
     * @param parameters
     */
    public ImagePicker(Parameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Κατασκευαστής κλάσης
     * @param parameters
     * @param url
     * @param document
     */
    public ImagePicker(Parameters parameters,  String url, Document document) {
        this.parameters = parameters;
        this.imageList = new ArrayList<>();
        this.document = document;
        this.url = url;
    }

    /**
     * Εκτελεί διαδικασία συλλογής εικόνων από όλες τις διαθέσιμες πηγές
     * @return λίστα με αντικείμενα Images
     */
    public List<Images> getImages(){
        Elements images = document.select("img");

        for(Element img : images){

            String imgURL = "";

            String[] attributes = {"src" , "data-src","data-lazy", "data-original", "srcset", "data-srcset"};

            for(String attr : attributes){
                if( img.hasAttr(attr)){
                    imgURL = img.absUrl(attr);
                    if(attr.contains("srcset") && imgURL.contains(" ")){
                        imgURL = imgURL.split("\\s+")[0];
                    }
                }
            }
            if (imgURL == null || imgURL.isEmpty() || !isImage(imgURL)) {
                    continue;
            }

            String caption = extractCaption(img);

            Images image = new Images(url, imgURL, caption);

            checkDuplicateImage( image);
            }

            catchImageFromStyle();
            searchInsideJson();

        return imageList;
    }

    /**
     * Εντοπίζει τις εικόνες που έχουν οριστεί ως background
     */
    private void catchImageFromStyle(){

        Elements allElementsWithStyle = document.select("[style*=background-image]");

        for (Element el : allElementsWithStyle) {

            String attr = "style";
            String style = el.attr(attr);

            Pattern pattern = Pattern.compile("url\\(['\"]?(.*?)['\"]?\\)");
            Matcher matcher = pattern.matcher(style);

            if (matcher.find()) {
                String bgURL = matcher.group(1);

                if ( !bgURL.startsWith("http")) {
                    bgURL = el.absUrl(attr).replace(style, url) + bgURL;

                }

                if (!bgURL.isEmpty() && !bgURL.startsWith("data:") && isImage(bgURL)){
                    Images img = new Images(url, bgURL, "");
                    checkDuplicateImage(img);
                }
            }
        }
    }

    /**
     * Έλεγχος αν το url της εικόνας αντιστοιχεί σε συγκεκριμένο τύπο εικόνας
     * @param imageUrl, διεύθυνση εικόνας
     * @return true αν αντιστοιχεί σε εικόνα, διαφορετικά false
     */
     private boolean isImage(String imageUrl){
        String url = imageUrl.toLowerCase();

         if (url.contains("?")) {
             url = url.split("\\?")[0];
         }

        if(url.startsWith("http") && (url.endsWith(".jpg") || url.endsWith(".jpeg")|| url.endsWith(".png") || url.endsWith(".webp")
        || url.endsWith(".gif") || url.endsWith(".tiff") || url.endsWith("psd") || url.endsWith("svg"))){
            return true;
        }
        return false;
     }

    /**
     * Αναζητά urls εικόνων μέσα σε block <script>. Εικόνες που φορτώνουν από json
     */
    public void searchInsideJson(){

        Elements elements = document.select("script");
        Pattern pattern = Pattern.compile("https?://[^\\s\"'<>]+?\\.(?i)(jpg|jpeg|png|webp|gif|svg|tiff|psd)");
        for(Element script :elements){
            String scriptData = script.data();
            Matcher matcher = pattern.matcher(scriptData);
            while(matcher.find()){
                String foundURL = matcher.group();
                Images img = new Images(url, foundURL, "");
                checkDuplicateImage(img);
            }

        }
    }

    /**
     * Έλεγχος αν υπάρχουν η εικόνα (αντικείμενο Images) υπάρχει είδη στη λίστα, για να μην εμφανιστεί δύο φορές.
     * @param image, αντικείμενο Images
     */

    private void checkDuplicateImage(Images image){
        if(!imageList.contains(image)) {
            imageList.add(image);
        }
    }

    /**
     * Εξαγωγή λεζάντας εικόνας.
     * @param element, η εικόνα (αντικείμενο Images)
     * @return λεζάντα εικόνας, Αν δεν υπάρχει λεζάντα θα επιστρέψει "".
     */
    private String extractCaption(Element element){
        String caption = "";

        Element fig = element.closest("figure");

        if(fig != null ){
            Element figCaption = fig.selectFirst("figcaption");
            if(figCaption != null){
                caption = figCaption.text().trim();
            }
        }

        if(caption.isEmpty() && element.hasAttr("alt")){
            caption = element.attr("alt").trim();
        }

        return caption;
    }
}

