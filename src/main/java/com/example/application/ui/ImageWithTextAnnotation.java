package com.example.application.ui;

import com.example.application.backEnd.annotation.DataForAnnotation;
import com.example.application.backEnd.annotation.FastAPIManager;
import com.example.application.backEnd.annotation.PipeLineForStanford;
import com.example.application.backEnd.annotation.StanfordCoreAnnotate;
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
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;

import java.io.Serial;
import java.util.*;

/**
 * Στην κλάση ImageWithTextAnnotation ο χρήστης μπορεί να συνδέσει μια εικόνα με ένα κείμενο
 * επισήμανσης. Ο χρήστης για μια εικόνα μπορεί να γράψει ένα μικρό κείμενο ή να χρησιμοποιήσει
 * τη λεζάντα της εικόνας για επισήμανση κειμένου.
 */
@Route(value = ImageWithTextAnnotation.ROUTE, layout = MainLayout.class)
public class ImageWithTextAnnotation extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "Image_Annotation";

    private final ServiceData serviceData;
    private final Parameters parameters;
    private final FastAPIManager fastAPIManager;
    private final PipeLineForStanford pipeLineForStanford;
    private final HandledProjects handledProjects;

    private ComboBox<String> urlComboBox = new ComboBox<>();
    private Button load;
    private Button annotate = new Button();
    private Button saveChanges = new Button();
    private Button addLabel = new Button();
    private List<Images> imageForAnnotation = new ArrayList<>();
    private Grid<WebPageImageAddText> grid;
    private ListDataProvider<WebPageImageAddText> dataProvider;
    private List<WebPageImageAddText> datalist = new ArrayList<>();
    private List<WebPageImageAddText> selectionForSave = new ArrayList<>();
    private List<String> strURL = new ArrayList<>();
    private String checkUrl = "";
    private Set<WebPageImageAddText> imageSelected = new HashSet<>();
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
     * @param pipeLineForStanford διαχείριση του pipeline για το Stanford CoreNLP (Application Scope)
     */
    public ImageWithTextAnnotation(ServiceData serviceData, Parameters parameters, FastAPIManager fastAPIManager,
                          HandledProjects handledProjects, PipeLineForStanford pipeLineForStanford){
        this.serviceData = serviceData;
        this.parameters = parameters;
        this.fastAPIManager = fastAPIManager;
        this.handledProjects = handledProjects;
        this.pipeLineForStanford = pipeLineForStanford;

        setPadding(true);
        setSpacing(true);
        setMargin(true);
        setWrap(false);
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        H2 title = new H2(  getTranslation("ImageWithText.H2.title")+ " " + handledProjects.getProjectName());

        HorizontalLayout header = setHeader(new HorizontalLayout());

        HorizontalLayout footer = setFooter(new HorizontalLayout());

        VerticalLayout body = setBody(new VerticalLayout());

        add(title, header, body, footer);
    }

    /**
     *Η μέθοδος δημιουργεί το πάνω μέρος της σελίδας, όπου ο χρήστης επιλέγει την ιστοσελίδα που θέλει να κάνει
     * επισήμανση. Επίσης, πληροφορεί τον χρήστη ποιο μοντέλο θα χρησιμοποιηθεί για να
     * γίνει επισήμανση. Αν σε περίπτωση που φορτώνεται μια σελίδα δεν εμφανιστούν αποτελέσματα
     * (δυναμική σελίδα) τότε μπορεί να επιλέξει "Προηγμένη συλλογή δεδομένων" για να
     * πραγματοποιηθεί η συλλογή δεδομένων με το PlayWright. Επιπλέον, υπάρχει η δυνατότητα
     * παράκαμψης των πιστοποιητικών SSL.
     * @param header το layout υπο διαμόρφωση
     * @return το διαμορφωμένο layout
     */
    private HorizontalLayout setHeader(HorizontalLayout header){

        strURL = serviceData.getStringURLAddress();
        createComboBox(urlComboBox, getTranslation("ImageWithText.comboUrl.label"), strURL);

        load = new Button(getTranslation("ImageWithText.Button.load"), e->{

            checkUrl =  urlComboBox.getValue();

            dataProvider.refreshAll();
            if(checkUrl == null) {
                Notification notification = Notification.show(getTranslation("ImageWithTextAnnotation.nullURL"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }
            validURL(checkUrl);
        });

        model = new TextField();

        model.setValue(parameters.getTextModel());
        model.setWidth("200px");
        model.setReadOnly(true);
        model.setLabel(getTranslation("ImageWithText.textField.model"));

        deepScrapping.setValue(parameters.isDeepScraping());
        deepScrapping.setReadOnly(false);
        deepScrapping.setLabel(getTranslation("ImageWithText.checkBox.deepScraping"));
        deepScrapping.setTooltipText(getTranslation("UrlsImageWithTextAnnotation.checkBox.toolTip"));
        deepScrapping.addValueChangeListener(e->{
            boolean value = e.getValue();
            parameters.setDeepScraping(value);
        });

        ignoreSSL.setValue(parameters.isIgnoreSSL());
        ignoreSSL.setReadOnly(false);
        ignoreSSL.setLabel(getTranslation("ImageWithText.checkBox.ignoreSSL"));
        ignoreSSL.setTooltipText(getTranslation("ImageWithText.checkBox.ignoreSSL.toolTip"));
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
     * Δημιουργείται το κάτω μέρος της σελίδας, το οποίο περιλαμβάνει τα κουμπιά:
     * "Επισήμανση", όπου ξεκινά η διαδικασία επισήμανσης του κειμένου
     * "Προσθήκη ετικέτας", εμφανίζεται ένα παράθυρό διαλόγου όπου ο χρήστης μπορεί να προσθέσει οντότητες
     * "Αποθήκευση", για την αποθήκευση όλων των επισημάνσεων στο αρχείο json
     * @param footer το layout υπο διαμόρφωση
     * @return το διαμορφωμένο layout
     */
    private HorizontalLayout setFooter(HorizontalLayout footer){

        createAnnotation(annotate, getTranslation("ImageWithText.Button.annotate"));

        createSave(saveChanges, getTranslation("ImageWithText.Button.save"));
        HorizontalLayout footerLeft= new HorizontalLayout(annotate, addLabelToDictionary(addLabel, getTranslation("ImageWithText.Button.addLabel")));
        HorizontalLayout footerRight = new HorizontalLayout(saveChanges);
        footerRight.getStyle().set("margin-left", "auto");
        footerRight.setSpacing(true);
        footer.setPadding(true);
        footer.add(footerLeft, footerRight);

        return footer;
    }

    /**
     * Δημιουργία το κύριο σώμα της σελίδας που περιέχει τον πίνακας με εικόνες και επισημάνσεις
     * @param body  layout υπο διαμόρφωση
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
     * Δημιουργία πίνακα με τις Επισημάνσεις. Ο πίνακας περιέχει αντικείμενα WebPageImageAddText
     * και οι στήλες του περιέχουν τις πληροφορίες id, εικόνα, Διεύθυνση, Κείμενο, Επισήμανση και Επισήμανση χρήστη.
     * Ο χρήστης μπορεί να προσθέσει κείμενο στη στήλη "Κείμενο" για τις εικόνες που επιθυμεί να γίνει επισήμανση.
     * Επιλέγοντας την αντίστοιχη εικόνα, στη στήλη επισήμανση εμφανίζεται η απάντηση του μοντέλου.
     * Επιπλέον, ο χρήστης μπορεί να προσθέσει τη δική του επισήμανση μέσω μια λίστα ετικετών (MultiSelectComboBox).
     * Τέλος, για καλύτερη εμπειρία χρήστη υπάρχει η δυνατότητα με click στην εικόνα αυτή να μεγεθύνεται
     */
    private void createGrid(){
        grid = new Grid<>(WebPageImageAddText.class, false);
        grid.setEmptyStateText(getTranslation("ImageWithText.grid.emptyGrid"));

        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn(BaseAnnotation::getId).setHeader(getTranslation("ImageWithText.grid.id")).setComparator(BaseAnnotation::compareTo).setSortable(true).setWidth("50px").setResizable(true);
        grid.addComponentColumn(e->{
            return zoomImage(e);
        }).setHeader(getTranslation("ImageWithText.grid.image")).setAutoWidth(false).setWidth("100px");

        grid.addColumn(WebPageImageAddText::getUrl).setHeader(getTranslation("ImageWithText.grid.url")).setSortable(true).setWidth("100px").setResizable(true);
        Grid.Column<WebPageImageAddText> txt = grid.addColumn(WebPageImageAddText::getText).setHeader(getTranslation("ImageWithText.grid.text")).setSortable(true).setWidth("350px").setResizable(true);

        grid.addComponentColumn(item -> {
            VerticalLayout layout = new VerticalLayout();
            layout.setPadding(false);
            layout.setSpacing(false);

            for(String e : item.getAnnotation()){
                Span span = new Span(e);
                span.getStyle().set("font-size", "var(--lumo-font-size-s");
                layout.add(span);
            }
            return  layout;
        }).setHeader(getTranslation("ImageWithText.grid.annotation")).setSortable(true).setWidth("400px").setResizable(true);

        grid.addComponentColumn(item -> {
            return createMultiSelectionComboBox(item, new MultiSelectComboBox<>(), serviceData.getStringListWord());

        }).setHeader(getTranslation("ImageWithText.grid.userAnnotation")).setWidth("400px").setResizable(true);

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

        Editor<WebPageImageAddText> editor = grid.getEditor();
        Binder<WebPageImageAddText> binder = new Binder<>(WebPageImageAddText.class);
        editor.setBinder(binder);

        TextField editField = new TextField();
        editField.setWidthFull();
        binder.forField(editField).bind(WebPageImageAddText::getText, WebPageImageAddText::setText);
        txt.setEditorComponent(editField);
        grid.addItemClickListener(e->{
            editor.editItem(e.getItem());
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
     * @param item αντικείμενο WebPageImageAddText
     * @param comboBox το MultiSelectComboBox που θα δημιουργηθεί
     * @param list λίστα με τις ετικέτες
     * @return το MultiSelectComboBox
     */
    private MultiSelectComboBox<String> createMultiSelectionComboBox(WebPageImageAddText item,  MultiSelectComboBox<String>comboBox, List<String> list){

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
     * Έλεγχος εγκυρότητας του url και ξεκινά η διαδικασία συλλογής εικόνων (scraping)
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
                }catch (Exception e){
                    imageForAnnotation = null;
                }

                if(imageForAnnotation != null) {
                    createWedPageImageFromImages();
                    urlComboBox.removeClassName("error");
                    urlComboBox.setInvalid(false);
                }
                else{
                    Notification notification = Notification.show(getTranslation("ImageWithText.errorExportImage"), 3000, Notification.Position.BOTTOM_START);
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
                    Notification.show(getTranslation("ImageWithText.Notification.foundImage"), 3000, Notification.Position.BOTTOM_START);

                }
            }
        }
        else{
            urlComboBox.addClassName("error");
            urlComboBox.setInvalid(true);
            urlComboBox.setErrorMessage(getTranslation("ImageWithText.comboBox.errorMessage"));
        }
    }

    /**
     * Τροποποιεί τα αντικείμενα τύπου Images σε αντικείμενα WebPageImageAddText
     * και τα προσθέτει στον πίνακα
     */
    private void createWedPageImageFromImages(){

        for(Images img : imageForAnnotation){
            WebPageImageAddText webPageImageAddText = new WebPageImageAddText(urlComboBox.getValue(), img.getUrlImage(), img.getCaption(), new ArrayList<>(), new ArrayList<>(), serviceData);
            datalist.add(webPageImageAddText);
        }
    }

    /**
     * Προσθήκη νέων ετικετών για τις επισημάνσεις που κάνει ο χρήστης. Η προσθήκη γίνεται
     * μέσω ενός παράθυρου διαλόγου. Μετά την προσθήκη ενημερώνεται το αρχείο ετικετών
     * και ανανεώνεται ο πίνακας.
     * @param btn το κουμπί που θα διαμορφωθεί
     * @param label η νέα ετικέτα
     * @return το κουμπί
     */
    private Button addLabelToDictionary(Button btn, String label){
        btn.setText(label);
        btn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btn.addClickListener(e-> {
            Dialog dialog = new Dialog();
            dialog.setDraggable(true);
            dialog.setCloseOnEsc(true);
            dialog.setHeaderTitle(getTranslation("ImageWithText.dialogHeader.title"));

            VerticalLayout layout = new VerticalLayout();

            textField.setReadOnly(false);
            textField.addKeyPressListener(key->{
               if(key.getKey().matches("Enter")){
                   saveLabelToDictionary();
               }
            });
            layout.add(textField);

            dialog.add(layout);
            Button add = new Button(getTranslation("ImageWithText.dialogButton.add"));
            add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            add.addClickListener(click -> {
                saveLabelToDictionary();
            });
            Button close = new Button(getTranslation("ImageWithText.dialogButton.close"));
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
     * για τις επιλεγμένες γραμμές του πίνακα σύμφωνα με το μοντέλο του χρήστη.
     * Αν ο FastAPI server δεν απαντήσει εντός 2 λεπτών, τότε θεωρείται ότι το
     * αίτημα είναι timeout και γίνεται επανεκκίνηση του server.
     * @param btn το κουμπί της επισήμανσης
     * @param label το label του κουμπιού
     */
    private void createAnnotation(Button btn, String label){
        btn.setText(label);
        btn.setEnabled(true);
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.addClickListener( e->{

            if(checkUrl == null || checkUrl.isBlank()){
                Notification notification = Notification.show(getTranslation("ImageWithTextAnnotation.nullURL"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            if (grid.getSelectedItems().isEmpty()) {
                Notification notification= Notification.show(getTranslation("ImageWithText.notSelectImage"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
            else {
                Notification notification = Notification.show(getTranslation("ImageWithText.selectedImage"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                imageSelected = grid.getSelectedItems();

                Map<String, List<String>> mapAnnotation = new HashMap<>();

                for(WebPageImageAddText image : imageSelected) {
                    String url = image.getImagePath();
                    String txtForAnnotation = image.getText();
                    if(txtForAnnotation == null || txtForAnnotation.isBlank()){
                        Notification notification_ = Notification.show(getTranslation("ImageWithText.withoutText"), 3000, Notification.Position.BOTTOM_START);
                        notification_.addThemeVariants(NotificationVariant.LUMO_WARNING);
                        continue;
                    }
                    if (parameters.getStanfordCoreNLP()) {
                        StanfordCoreAnnotate stanfordCoreNLP = new StanfordCoreAnnotate();
                        List<String> tempListStanford = new ArrayList<>();
                        tempListStanford = stanfordCoreNLP.getStanfordCoreAnnotate(pipeLineForStanford.getPipeline(), txtForAnnotation);
                        mapAnnotation.put(url, tempListStanford);
                    }
                    else if(parameters.getSpacy()){
                        Map<String, List<String>> tempmap = new HashMap<>();

                        if(fastAPIManager.isServerAlive()){
                            DataForAnnotation dataForAnnotation = new DataForAnnotation(txtForAnnotation);
                            tempmap = fastAPIManager.annotate("TextAnnotation", dataForAnnotation);
                        }
                        else{

                            DataForAnnotation dataForAnnotation = new DataForAnnotation(txtForAnnotation);
                            tempmap = fastAPIManager.annotate("TextAnnotation", dataForAnnotation);
                        }

                        if(tempmap.containsKey("timeout")){
                            Notification notification_ = Notification.show(getTranslation("ImageWithText.timeout"),3000, Notification.Position.BOTTOM_START);
                            notification_.addThemeVariants(NotificationVariant.LUMO_WARNING);

                            grid.deselectAll();
                            return;
                        }
                        if(tempmap.containsKey("exception")){
                            Notification notification_ = Notification.show(getTranslation("ImageWithText.exception"),3000, Notification.Position.BOTTOM_START);
                            notification_.addThemeVariants(NotificationVariant.LUMO_WARNING);
                            grid.deselectAll();
                            return;
                        }
                        List<String> tempList = new ArrayList<>();
                        tempList = fastAPIManager.convertData(tempmap);

                        mapAnnotation.put(url, tempList);

                    }
                }

                for(WebPageImageAddText webPageImage : imageSelected){
                    String url = webPageImage.getImagePath();
                    if(mapAnnotation.containsKey(url)){
                        webPageImage.setAnnotation(mapAnnotation.get(url));
                        dataProvider.refreshItem(webPageImage);
                    }
                }

            }
            grid.deselectAll();

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
                Notification notification = Notification.show(getTranslation("ImageWithText.notSelectedRecord"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
            else {

                HandledJsonFile jsonFile = new HandledJsonFile(handledProjects);
                jsonFile.initHandledJsonFile();
                List<BaseAnnotation> base = new ArrayList<>(selectionForSave);
                boolean flagSave = true;

                for (BaseAnnotation b : base) {
                    if(b.getImagePath().startsWith("http")) {
                        String path = jsonFile.saveImage((WebPageImageAddText) b, parameters.isIgnoreSSL());
                        if (path.equals("false")) {
                            Notification notification = Notification.show(getTranslation("ImageWithText.errorSaveImage"),3000, Notification.Position.BOTTOM_START);
                            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                            grid.deselectAll();
                            flagSave = false;
                            continue;
                        }
                        ((WebPageImageAddText) b).setImagePath(path);
                    }
                }

                if (flagSave && jsonFile.saveBaseAnnotationNew(base)) {
                    Notification notification = Notification.show(getTranslation("ImageWithText.saveChanges"),3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    grid.deselectAll();
                } else {
                    Notification notification = Notification.show(getTranslation("ImageWithText.errorSaveChanges"),3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    grid.deselectAll();
                }
            }});

        btn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btn.getStyle().set("margin-inline-start", "auto");
    }

    /**
     * Διαχειρίζεται την προβολή και τη μεγέθυνση της εικόνας μέσω ενός παράθυρου διαλόγου,
     * όταν ο χρήστης κάνει click. Αν η εικόνα δεν ξεκινά με http τότε τροποποιείται το μονοπάτι
     * προσθέτοντας /images/. Αυτό επιτρέπει τον browser να παρακάμψει περιορισμούς ασφαλείας για
     *  απευθείας πρόσβαση στο σύστημα αρχείων.
     *  @param item αντικείμενο WebPageImageAddText
     *  @return εικόνα
     */
    private Component zoomImage(WebPageImageAddText  item){
        Image image = new Image();
        if(item.getImagePath().equals("") || !item.getImagePath().startsWith("http")){
            Icon icon = LumoIcon.PHOTO.create();
            return icon;
        }
        String path = item.getImagePath();
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

            Notification.show(getTranslation("ImageWithText.image.selectedImage"),3000, Notification.Position.BOTTOM_START);
        });
        return image;
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
            Notification notification = Notification.show(getTranslation("ImageWithText.dialogNotification.save"),3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            grid.getDataProvider().refreshAll();

        } else {
            Notification notification = Notification.show(getTranslation("ImageWithText.dialogNotification.notSave"),3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

}
