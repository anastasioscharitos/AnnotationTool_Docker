package com.example.application.ui;

import com.example.application.backEnd.annotation.DataForAnnotation;
import com.example.application.backEnd.annotation.FastAPIManager;
import com.example.application.backEnd.dataHandling.*;
import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.handledFiles.HandledJsonFile;
import com.example.application.backEnd.scraping.*;
import com.example.application.backEnd.settings.Parameters;
import com.example.application.backEnd.uploadData.ServiceData;
import com.example.application.backEnd.handledFiles.UploadSaveData;
import com.example.application.backEnd.uploadData.Word;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;

import java.io.Serial;
import java.util.*;

/**
 * Η κλάση UrlsImageAnnotation είναι υπεύθυνη για την επισήμανση εικόνων.
 * Η εφαρμογή φορτώνει τις εικόνες από επιλεγμένη σελίδα. Ο χρήστης επιλέγει
 * εικόνες τις οποίες αποστέλλονται στον FastAPI server για επισήμανση.
 * Επιπλέον, ο χρήστης έχει τη δυνατότητα να εισάγει τη δική του επισήμανση
 * από μια λίστα ετικετών. Τέλος, τα αποτελέσματα αποθηκεύονται τοπικά.
 */
@Route(value = UrlsImageAnnotation.ROUTE, layout = MainLayout.class)
public class UrlsImageAnnotation extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "Urls_Image_Annotation";

    private final ServiceData serviceData;
    private final Parameters parameters;
    private final FastAPIManager fastAPIManager;
    private final HandledProjects handledProjects;

    private ComboBox<String> urlComboBox = new ComboBox<>();
    private Button load;
    private Button annotate = new Button();
    private Button saveChanges = new Button();
    private Button addLabel = new Button();
    private List<Images> imageForAnnotation = new ArrayList<>();
    private Grid<WebPageImage> grid;
    private ListDataProvider<WebPageImage> dataProvider;

    private List<WebPageImage> datalist = new ArrayList<>();
    private List<WebPageImage> selectionForSave = new ArrayList<>();
    private  List<String> strUrl = new ArrayList<>();
    private String checkUrl = "";
    private Set<WebPageImage> imageSelected = new HashSet<>();
    private TextField model;
    private Checkbox deepScrapping = new Checkbox();
    private Checkbox ignoreSSL = new Checkbox();
    private TextField textField = new TextField();

    /**
     * Κατασκευαστής κλάσης
     * @param serviceData διαχείριση δεδομένων (Session scope)
     * @param parameters διαχείριση παραμέτρων επισήμανσης (Session scope)
     * @param fastAPIManager διαχείριση του FastAPI server (Application scope)
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     */
    public UrlsImageAnnotation(ServiceData serviceData, Parameters parameters, FastAPIManager fastAPIManager, HandledProjects handledProjects) {
        this.serviceData = serviceData;
        this.parameters = parameters;
        this.fastAPIManager = fastAPIManager;
        this.handledProjects = handledProjects;

        setPadding(true);
        setSpacing(true);
        setMargin(true);
        setWrap(false);
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        H2 title = new H2(getTranslation("UrlsImageAnnotation.H2.title") + " " + handledProjects.getProjectName());

        HorizontalLayout header = setHeader(new HorizontalLayout());

        HorizontalLayout footer = setFooter(new HorizontalLayout());

        VerticalLayout body = setBody(new VerticalLayout());

        add(title, header, body, footer);
    }

    /**
     * Η μέθοδος δημιουργεί το πάνω μέρος της σελίδας, όπου ο χρήστης επιλέγει
     * την ιστοσελίδα που θέλει να κάνει επισήμανση. Εμφανίζεται το μοντέλο επισήμανσης.
     * Ελέγχει τη διαδικασία εξαγωγής του κειμένου από τις σελίδες, scraping mode.
     * Επιπλέον, υπάρχει η δυνατότητα παράκαμψης των πιστοποιητικών SSL.
     * @param header το layout υπο διαμόρφωση
     * @return το διαμορφωμένο layout
     */
    private HorizontalLayout setHeader(HorizontalLayout header){

        strUrl = serviceData.getStringURLAddress();
        createComboBox(urlComboBox, getTranslation("UrlsImageAnnotation.comboUrl.label"), strUrl);

        load = new Button(getTranslation("UrlsImageAnnotation.button.load"), e->{
            checkUrl =  urlComboBox.getValue();

            dataProvider.refreshAll();
            if(checkUrl == null) {
                Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.nullURL"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            validURL(checkUrl);

        });

        model = new TextField();

        model.setValue(parameters.getImageModel());
        model.setWidth("200px");
        model.setReadOnly(true);
        model.setLabel(getTranslation("UrlsImageAnnotation.textField.model"));

        deepScrapping.setValue(parameters.isDeepScraping());
        deepScrapping.setReadOnly(false);
        deepScrapping.setLabel(getTranslation("UrlsImageAnnotation.checkBox.deepScraping"));
        deepScrapping.setTooltipText(getTranslation("UrlsImageAnnotation.checkBox.toolTip"));
        deepScrapping.addValueChangeListener(e->{
            boolean value = e.getValue();
            parameters.setDeepScraping(value);
        });

        ignoreSSL.setValue(parameters.isIgnoreSSL());
        ignoreSSL.setReadOnly(false);
        ignoreSSL.setLabel(getTranslation("UrlsImageAnnotation.checkBox.ignoreSSL"));
        ignoreSSL.setTooltipText(getTranslation("UrlsImageAnnotation.checkBox.ignoreSSL.toolTip"));
        ignoreSSL.addValueChangeListener(e->{
            boolean value = e.getValue();
            parameters.setIgnoreSSL(value);
        });

        header.add(urlComboBox, load, model, deepScrapping, ignoreSSL);
        header.setAlignItems(Alignment.BASELINE);
        header.getThemeList().clear();
        header.setSpacing(true);
        header.setPadding(true);

        return header;

    }

    /**
     * Δημιουργία του κάτω μέρους της σελίδας, το οποίο περιλαμβάνει τα κουμπιά:
     * "Επισήμανση", όπου ξεκινά η διαδικασία επισήμανσης των επιλεγμένων εικόνων
     * "Προσθήκη ετικέτας", εμφανίζεται ένα παράθυρό διαλόγου όπου ο χρήστης μπορεί να προσθέσει ετικέτες
     * "Αποθήκευση", για την αποθήκευση των επιλεγμένων επισημάνσεων στο αρχείο json
     * @param footer το layout υπο διαμόρφωση
     * @return το διαμορφωμένο layout
     */
    private HorizontalLayout setFooter(HorizontalLayout footer){

        createAnnotation(annotate, getTranslation("UrlsImageAnnotation.button.annotate"));

        createSave(saveChanges, getTranslation("UrlsImageAnnotation.button.save"));
        HorizontalLayout footerLeft= new HorizontalLayout(annotate, addLabelToDictionary(addLabel, getTranslation("UrlsImageAnnotation.button.addLabel")));
        HorizontalLayout footerRight = new HorizontalLayout(saveChanges);
        footerRight.getStyle().set("margin-left", "auto");
        footerRight.setSpacing(true);
        footer.setPadding(true);
        footer.add(footerLeft, footerRight);

        return footer;
    }

    /**
     * Δημιουργεί το κύριο σώμα της σελίδας. Περιέχει πίνακα με τις εικόνες της σελίδας
     * @param body layout υπο διαμόρφωση
     * @return το διαμορφωμένο layout
     */
    private VerticalLayout setBody(VerticalLayout body) {

        createGrid();
        body.getStyle().set("flex-grow", "1");
        body.getStyle().set("min-height", "0");
        body.add(grid);
        body.setSizeFull();

        return body;
    }

    /**
     * Δημιουργία πίνακα με τις επισημάνσεις. Ο πίνακας περιέχει αντικείμενα
     * WebPageImage και οι στήλες του περιέχουν πληροφορίες id, εικόνα, Διεύθυνση,
     * Κείμενο/Λεζάντα εικόνας, Επισήμανση και Επισήμανση χρήστη. Επισήμανση
     * πραγματοποιείται μόνο σε επιλεγμένες εικόνες. Επιπλέον, ο χρήστης μπορεί να
     * προσθέσει τη δική του Επισήμανση μέσω μια λίστα ετικετών (MultiSelectComboBox).
     * Τέλος, για καλύτερη εμπειρία χρήστη υπάρχει η δυνατότητα μεγέθυνσης
     * της εικόνας.
     */
    private void createGrid(){
        grid = new Grid<>(WebPageImage.class, false);
        grid.setEmptyStateText(getTranslation("UrlsImageAnnotation.grid.emptyGrid"));

        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn(BaseAnnotation::getId).setHeader(getTranslation("UrlsImageAnnotation.grid.id")).setComparator(BaseAnnotation::compareTo).setSortable(true).setWidth("50px").setResizable(true);
        grid.addComponentColumn(e->{
            return zoomImage(e);
        }).setHeader(getTranslation("UrlsImageAnnotation.grid.image")).setAutoWidth(false).setWidth("100px").setResizable(true);

        grid.addColumn(WebPageImage::getUrl).setHeader(getTranslation("UrlsImageAnnotation.grid.url")).setSortable(true).setWidth("100px").setResizable(true);
        grid.addColumn(WebPageImage::getText).setHeader(getTranslation("UrlsImageAnnotation.grid.caption")).setSortable(true).setWidth("350px").setResizable(true);

        grid.addComponentColumn(item -> {
            return createAnnotateLayout(item, new VerticalLayout());

        }).setHeader(getTranslation("UrlsImageAnnotation.grid.annotation")).setSortable(true).setWidth("400px").setResizable(true);

        grid.addComponentColumn(item -> {
            return createMultiSelectionComboBox(item, new MultiSelectComboBox<>(), serviceData.getStringListWord());

        }).setHeader(getTranslation("UrlsImageAnnotation.grid.userAnnotation")).setWidth("400px").setResizable(true);

        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        grid.addSelectionListener(selectionEvent -> {
            selectionForSave = selectionEvent.getAllSelectedItems().stream().toList();

            if (!selectionForSave.isEmpty() && selectionForSave != null) {
                saveChanges.setEnabled(true);
                annotate.setEnabled(true);
            } else {
                saveChanges.setEnabled(false);
                annotate.setEnabled(false);
            }
        });

        dataProvider = new ListDataProvider<>(datalist);

        grid.setDataProvider(dataProvider);
        grid.setHeightFull();
        grid.setAllRowsVisible(false);
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    }

    /**
     * Δημιουργία MultiSelectionComboBox. Στο πεδίο "Επισήμανση χρήστη", ο χρήστης
     * έχει δυνατότητα να επιλέξει ένα πλήθος ετικετών από μια λίστα.
     * @param item αντικείμενο WebPageImage
     * @param comboBox το MultiSelectComboBox που θα δημιουργηθεί
     * @param list λίστα με τις ετικέτες
     * @return το MultiSelectComboBox
     */
    private MultiSelectComboBox<String> createMultiSelectionComboBox(WebPageImage item,  MultiSelectComboBox<String>comboBox, List<String> list){

        comboBox.setItems(list);
        comboBox.setWidthFull();

        if(item.getUserAnnotation() != null && !item.getUserAnnotation().isEmpty()){
            comboBox.setValue(new HashSet<>(item.getUserAnnotation()));

        }

        comboBox.addValueChangeListener(e-> {
            List<String> annotation = new ArrayList<>(e.getValue());
            item.setUserAnnotation(annotation);
        });
        return comboBox;

    }

    /**
     * Αρχικοποιεί το ComboBox για την επιλογή διευθύνσεων URL
     * @param comboBox το comboBox που θα δημιουργηθεί
     * @param label το label του comboBox
     * @param listValue λίστα απο διευθύνσεις που θα πραγματοποιηθεί η επισήμανση
     */
    private void createComboBox(ComboBox<String> comboBox, String label, List<String> listValue){
        ListDataProvider<String> list = new ListDataProvider<>(listValue);
        comboBox.setLabel(label);
        comboBox.setItems(list);
        comboBox.getStyle().set("--vaadin-combo-box-overlay-width", "600px");
        comboBox.getStyle().setWidth("500px");
        comboBox.setAutoOpen(true);

    }

    /**
     * Στην επισήμανση που επιστρέφει το μοντέλο, θα πρέπει να πραγματοποιηθεί αναδίπλωση
     * για να χωρέσει στο αντίστοιχο κελί του πίνακα. Για την επίτευξη αυτού δημιουργείται
     * ένα Span όπου εισάγεται η επισήμανση κάθε αντικειμένου
     * @param item αντικείμενο BaseAnnotation (WebPageImage)
     * @param layout το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private VerticalLayout createAnnotateLayout(BaseAnnotation item, VerticalLayout layout){
        layout.setPadding(false);
        layout.setSpacing(false);

        for (String e : item.getAnnotation()) {
            Span span = new Span(e);
            span.getStyle().set("font-size", "var(--lumo-font-size-s");
            layout.add(span);
        }
        return layout;
    }

    /**
     * Έλεγχος εγκυρότητας του url. Αν είναι έγκυρο, ξεκινά η διαδικασία συλλογής εικόνων (scraping)
     * είτε με το jsoup είτε με το PlayWright
     * @param url η διεύθυνση που θα γίνει η επισήμανση
     */
    private void validURL(String url){
        CheckURL checkURL = new CheckURL(url);
        if(checkURL.isValid()) {
            datalist.clear();
            grid.deselectAll();
            imageForAnnotation.clear();

            if(parameters.isDeepScraping()){
                try(PlaywrightDeepScraping deepScraping = new PlaywrightDeepScraping()) {
                    imageForAnnotation = deepScraping.getListImagePlaywright(url);
                }
                catch (Exception e){
                    imageForAnnotation = null;
                }
                if(imageForAnnotation != null) {
                    createWedPageImageFromImages();
                    urlComboBox.removeClassName("error");
                    urlComboBox.setInvalid(false);
                }
                else{
                    Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.errorExportImage"), 3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                }
            }

            else {
                WebPagePicker webPagePicker = new WebPagePicker(parameters);
                WebPage webPage = webPagePicker.pickWebPage(url);

                if (webPage != null && !webPage.getImages().isEmpty()) {
                    imageForAnnotation = webPage.getImages();

                    createWedPageImageFromImages();
                    urlComboBox.removeClassName("error");
                    urlComboBox.setInvalid(false);

                } else {
                    Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.Notification.foundImage"), 3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                }
            }
        }
        else{
            urlComboBox.addClassName("error");
            urlComboBox.setInvalid(true);
            urlComboBox.setErrorMessage(getTranslation("UrlsImageAnnotation.comboBox.errorMessage"));
        }
    }

    /**
     *  Τροποποιεί τα αντικείμενα τύπου Images σε αντικειμένα WebPageImage
     *  και τα προσθέτει στον πίνακα
     */
    private void createWedPageImageFromImages(){

        for(Images img : imageForAnnotation){
            WebPageImage webPageImage = new WebPageImage(urlComboBox.getValue(), img.getUrlImage(), img.getCaption(), new ArrayList<>(), new ArrayList<>(), serviceData);
            datalist.add(webPageImage);
        }
    }

    /**
     * Προσθήκη νέων ετικετών για τις επισημάνσεις που κάνει ο χρήστης. Η προσθήκη γίνεται
     * μέσω ενός παράθυρου διαλόγου. Μετά την προσθήκη ενημερώνεται το αρχείο ετικετών
     *και ανανεώνεται ο πίνακας.
     * @param btn το κουμπί που θα διαμορφωθεί
     * @param label η ετικέτα
     * @return το κουμπί
     */
    private Button addLabelToDictionary(Button btn, String label){
        btn.setText(label);
        btn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btn.addClickListener(e-> {
            Dialog dialog = new Dialog();
            dialog.setDraggable(true);
            dialog.setCloseOnEsc(true);
            dialog.setHeaderTitle(getTranslation("UrlsImageAnnotation.dialogHeader.title"));

            VerticalLayout layout = new VerticalLayout();

            textField.setReadOnly(false);
            layout.add(textField);
            textField.addKeyPressListener(key ->{
               if(key.getKey().matches("Enter")){
                   saveLabelToDictionary();
               }
            });

            dialog.add(layout);
            Button add = new Button(getTranslation("UrlsImageAnnotation.dialogButton.add"));
            add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            add.addClickListener(click -> {
                saveLabelToDictionary();
            });
            Button close = new Button(getTranslation("UrlsImageAnnotation.dialogButton.close"));
            close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            close.addClickListener(click -> dialog.close());
            dialog.getFooter().add(add);
            dialog.getFooter().add(close);

            dialog.open();

        });

        return btn;
    }

    /**
     * Δημιουργία κουμπιού που εκτελεί την επισήμανση. Η επισήμανση πραγματοποιείται
     *  για τις επιλεγμένες γραμμές του πίνακα σύμφωνα με το μοντέλο του χρήστη.
     *  Αν ο FastAPI server δεν απαντήσει εντός 2 λεπτών, τότε θεωρείται ότι το
     *  αίτημα είναι timeout και γίνεται επανεκκίνηση του server. Επίσης, πριν την
     *  αποστολή των εικόνων γίνεται έλεγχος για να διασφαλιστεί ότι το μοντέλο
     *  που ζητά ο χρήστης να γίνει επισήμανση είναι το ίδιο με αυτό που είναι
     *  φορτωμένο στη μνήμη
     * @param btn κουμπί επισήμανσης
     * @param label του κουμπιού
     */
    private void createAnnotation(Button btn, String label){
        btn.setText(label);
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.setEnabled(true);
        btn.addClickListener( e->{
            if(checkUrl == null || checkUrl.isBlank()){
                Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.nullURL"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }
            if(!checkUrl.equals(urlComboBox.getValue())){
                Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.errorURL_Image"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }
            if(checkAnnotationModel()){

                grid.deselectAll();
           }
            else {
                if (grid.getSelectedItems().isEmpty()) {
                    Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.notSelectImage"), 3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                } else {
                    imageSelected = grid.getSelectedItems();

                    List<String> imgUrl = new ArrayList<>();
                    for (WebPageImage image : imageSelected) {
                        String url = image.getImagePath();
                        imgUrl.add(url);
                    }

                    DataForAnnotation dataForAnnotation = new DataForAnnotation(imgUrl, serviceData.getStringListWord(), parameters.getThreshold(), parameters.getTopK(), parameters.isIgnoreSSL());
                    Map<String, List<String>> mapAnnotation = fastAPIManager.annotate("ImageAnnotation", dataForAnnotation);

                    if (mapAnnotation.containsKey("timeout")) {
                        Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.timeout"), 4000, Notification.Position.BOTTOM_START);
                        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);

                        grid.deselectAll();
                        return;
                    }
                    if(mapAnnotation.containsKey("exception")) {
                        Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.exception"), 4000, Notification.Position.BOTTOM_START);
                        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                        grid.deselectAll();
                        return;
                    }

                    boolean hasErrorSSL = false;
                    for (WebPageImage webPageImage : imageSelected) {
                        String url = webPageImage.getImagePath();
                        if (mapAnnotation.containsKey(url)) {
                            if (mapAnnotation.get(url).stream().anyMatch(s ->
                                s.startsWith("Error")
                            )) {
                                hasErrorSSL = true;
                                webPageImage.setAnnotation(List.of(getTranslation("UrlsImageAnnotation.ErrorSSL")));
                                dataProvider.refreshItem(webPageImage);
                                continue;
                            }
                            webPageImage.setAnnotation(mapAnnotation.get(url));
                            dataProvider.refreshItem(webPageImage);
                        }
                    }
                    if(hasErrorSSL){
                        Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.exceptionSSL"), 4000, Notification.Position.BOTTOM_START);
                        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                    }
                    else {
                        Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.selectedImage"), 3000, Notification.Position.BOTTOM_START);
                        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                    }
                }
                grid.deselectAll();
            }
        });

    }

    /**
     * Δημιουργία του κουμπιού "Αποθήκευση". Η μέθοδος αποθηκεύει τοπικά την εικόνα
     * και τα δεδομένα στο αρχείο json.
     * Για τον έλεγχο αποθήκευσης των εικόνων χρησιμοποιείται ένας φάρος (flagSave)
     * όπου στην περίπτωση κατά την οποία δεν είναι δυνατή η αποθήκευση των εικόνων τότε
     * δεν αποθηκεύονται και τα υπόλοιπα στοιχεία.
     * @param btn κουμπί "Αποθήκευση"
     * @param label το label του κουμπιού
     */
    private void createSave(Button btn, String label){
        btn.setText(label);
        btn.addClickListener(e-> {
            if(grid.getSelectedItems().isEmpty()){
                Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.notSelectedRecord"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
            else {

                HandledJsonFile jsonFile = new HandledJsonFile(handledProjects);
                jsonFile.initHandledJsonFile();
                List<BaseAnnotation> base = new ArrayList<>(selectionForSave);
                boolean flagSave = true;
                for (BaseAnnotation b : base) {
                    if(b.getImagePath().startsWith("http")) {
                        String path = jsonFile.saveImage((WebPageImage) b, parameters.isIgnoreSSL());
                        if (path.equals("false")) {
                            Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.errorSaveChanges"),3000, Notification.Position.BOTTOM_START);
                            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                            grid.deselectAll();
                            flagSave = false;
                            continue;
                        }
                        else{
                            Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.saveChanges"),3000, Notification.Position.BOTTOM_START);
                            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        }
                        ((WebPageImage) b).setImagePath(path);
                    }
                    else{
                        Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.errorSaveChanges"),3000, Notification.Position.BOTTOM_START);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        grid.deselectAll();
                        flagSave = false;
                    }


                }

                if (flagSave && jsonFile.saveBaseAnnotationNew(base)) {
                    Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.SavedChanges"),3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    grid.deselectAll();
                } else {
                    Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.errorSavingChanges"),3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    grid.deselectAll();
                }
            }});

        btn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btn.getStyle().set("margin-inline-start", "auto");
    }

    /**
     * Διαχειρίζεται την προβολή και τη μεγέθυνση της εικόνας μέσω ενός παράθυρου διαλόγου,
     *  όταν ο χρήστης κάνει click. Αν η εικόνα δεν ξεκινά με http τότε τροποποιείται το μονοπάτι
     *  προσθέτοντας /images/. Αυτό επιτρέπει τον browser να παρακάμψει περιορισμούς ασφαλείας για
     *  απευθείας πρόσβαση στο σύστημα αρχείων.
     * @param item αντικείμενο WebPageImage
     * @return εικόνα
     */
    private Component zoomImage(WebPageImage  item){
        Image image = new Image();
        String path = item.getImagePath();
        if(path == null || path.isBlank() ){
            Icon icon = LumoIcon.PHOTO.create();
            return icon;
        }

        if(!path.startsWith("http")) {
            path =  "/images/" + item.getImagePath();
        }

        image.setSrc(path);
        image.setAlt("img");

        image.getStyle().set("max-width" , "100%");
        image.getStyle().set("max-height" , "100%");
        image.getStyle().set("object-fit" , "contain");

        image.addClickListener(img->{
            Dialog dialog = new Dialog();
            dialog.setCloseOnEsc(true);
            dialog.setCloseOnOutsideClick(true);
            dialog.setDraggable(true);
            String path_ = item.getImagePath();
            if(!path_.startsWith("http")) {
                path_ =  "/images/" + item.getImagePath();
            }
            Image  zoomImg = new Image(path_, "Full view");
            zoomImg.setWidth("600px");

            zoomImg.addDoubleClickListener(click -> dialog.close());
            dialog.add(zoomImg);
            dialog.open();

            Notification.show(getTranslation("UrlsImageAnnotation.image.selectedImage"),3000, Notification.Position.BOTTOM_START);});
        return image;
    }

    /**
     * Έλεγχος αν το μοντέλο που ζητά ο χρήστης για επισήμανση
     * είναι το ίδιο με το μοντέλο που είναι φορτωμένο στη μνήμη. Για να μην
     * καταναλώνονται πολλοί πόροι στη μνήμη είναι φορτωμένο μόνο ένα μοντέλο
     * επισήμανσης εικόνων. Έτσι, γίνεται αίτημα στον FastAPI server για το
     * μοντέλο που είναι φορτωμένο.
     * @return true αν το μοντέλο που χρησιμοποιεί ο χρήστης είναι το ίδιο με
     * του FastAPI server, διαφορετικά false
     */
    private boolean checkAnnotationModel(){
        Map<String, String> checkModel = fastAPIManager.getStatus("Status");

        if(checkModel.containsKey("errorStatus")){

            return false;
        }
        if(checkModel.containsKey("timeout")){
            Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.timeout"), 4000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);

            return false;
        }
        String fastAPIModel = checkModel.get("image");
       System.out.println(fastAPIModel);

        if(!fastAPIModel.equals(model.getValue().toLowerCase())){
            loadModelDialog(new Dialog(), fastAPIModel).open();
            return true;
        }
        return false;
    }

    /**
     * Δημιουργία παράθυρου διαλόγου για το αν ο χρήστης επιθυμεί ο FastAPI
     * server να φορτώσει το δικό του μοντέλο επισήμανσης εικόνων ή να κάνει
     * επισήμανση με το μοντέλο που είναι στη μνήμη
     * @param loadModel παράθυρο διαλόγου
     * @param modelInMemory μοντέλο στη μνήμη
     * @return το διαμορφωμένο παράθυρο διαλόγου
     */
    private Dialog loadModelDialog(Dialog loadModel, String modelInMemory){
        loadModel.setDraggable(true);
        loadModel.setCloseOnEsc(true);
        loadModel.setHeaderTitle(getTranslation("UrlsImageAnnotation.loadModel"));

        VerticalLayout layout = new VerticalLayout();

        Span message = new Span();
        message.setText(getTranslation("UrlsImageAnnotation.loadModelMessage1") + modelInMemory+ getTranslation("UrlsImageAnnotation.loadModelMessage2"));

        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setWidth("400px");

        layout.add(message);

        loadModel.add(layout);
        Button load = new Button(getTranslation("UrlsImageAnnotation.loadModelButton"));
        load.setTooltipText(getTranslation("UrlsImageAnnotation.toolTip.loadModelButton") + " " + model.getValue());
        load.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        load.addClickListener( e->{

            String imageModel = parameters.getImageModel().toLowerCase();
            Notification.show(getTranslation("UrlsImageAnnotation.loadModelOK")  + imageModel, 3000, Notification.Position.BOTTOM_START);
            Map<String, String> modelType = fastAPIManager.loadModel(imageModel, "Update_Settings");

            if(modelType.containsKey("errorLoad") || modelType.containsKey("timeout")){
                Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.errorLoadModelReady"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                loadModel.close();
            }

            String txt = fastAPIManager.convertModel(modelType);
            Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.loadModelReady")  + txt, 3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            loadModel.close();
        });

        Button cancelButton = new Button(getTranslation("UrlsImageAnnotation.cancelModelButton"));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addClickListener(e->{
            loadModel.close();
        });

        Button existingModel = new Button(getTranslation("UrlsImageAnnotation.existingModelButton"));
        existingModel.setTooltipText("UrlsImageAnnotation.toolTip.existingModelButton");
        existingModel.addThemeVariants(ButtonVariant.LUMO_WARNING);
        existingModel.addClickListener(e->{
            String existing = modelInMemory.toUpperCase();
            model.setValue(existing);
            Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.existingModelReady")  + existing, 3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            loadModel.close();

        });

        loadModel.getFooter().add(load);
        loadModel.getFooter().add(existingModel);
        loadModel.getFooter().add(cancelButton);
        return loadModel;
    }

    /**
     * Η μέθοδος αποθηκεύει τα labels στο λεξικό. Το label από το textField δεν
     * πρέπει να είναι κενό και αποθηκεύεται στο αρχείο του λεξικού του έργου εμφανίζοντας
     * μήνυμα επιτυχίας, διαφορετικά εμφανίζεται μήνυμα αποτυχίας
     */
    private  void saveLabelToDictionary(){
        String text = textField.getValue();
        Word word = new Word(text);
        serviceData.addWord(word);
        textField.setValue("");

        UploadSaveData uploadSaveData = new UploadSaveData(handledProjects);
        uploadSaveData.initUploadSaveData();

        if (!text.isBlank() && uploadSaveData.saveWords(serviceData.getWord())) {
            Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.dialogNotification.save"),3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            grid.getDataProvider().refreshAll();

        } else {
            Notification notification = Notification.show(getTranslation("UrlsImageAnnotation.dialogNotification.notSave"),3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

}
