package com.example.application.ui;

import com.example.application.backEnd.annotation.PipeLineForStanford;
import com.example.application.backEnd.annotation.StanfordCoreAnnotate;
import com.example.application.backEnd.annotation.DataForAnnotation;
import com.example.application.backEnd.annotation.FastAPIManager;
import com.example.application.backEnd.dataHandling.BaseAnnotation;
import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.handledFiles.HandledJsonFile;
import com.example.application.backEnd.dataHandling.WebPageText;
import com.example.application.backEnd.scraping.*;
import com.example.application.backEnd.settings.Parameters;
import com.example.application.backEnd.uploadData.ServiceData;
import com.example.application.backEnd.handledFiles.UploadSaveData;
import com.example.application.backEnd.uploadData.Word;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.io.Serial;
import java.util.*;

/**
 * Η κλάση UrlsTextAnnotation είναι υπεύθυνη για την επισήμανση κειμένου.
 * Η εφαρμογή φορτώνει το κείμενο απο επιλεγμένη σελίδα. Ο χρήστης επιλέγει
 * κείμενο με το ποντίκι και το στέλνει για επεξεργασία είτε μέσω του FastAPI server
 * (μοντέλο spaCy) είτε μέσω του Stanford CoreNLP pipeline.
 * Επιπλέον, ο χρήστης έχει τη δυνατότητα να εισάγει τη δική του επισήμανση
 * από μια λίστα ετικετών.Τέλος, τα αποτελέσματα αποθηκεύονται τοπικά.
 */
@Route(value = UrlsTextAnnotation.ROUTE, layout = MainLayout.class)
public class UrlsTextAnnotation extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "Urls_Text_Annotation";

    private final ServiceData serviceData;
    private final Parameters parameters;
    private final FastAPIManager fastAPIManager;
    private final PipeLineForStanford pipelineService;
    private final HandledProjects handledProjects;

    private ComboBox<String> urlComboBox = new ComboBox<>();
    private Button load;
    private Button addLabel = new Button();
    private Button annotate = new Button();
    private Button saveChanges = new Button();
    private Div textForAnnotation;
    private Grid<WebPageText> grid;
    private ListDataProvider<WebPageText> dataProvider;
    private String selectedText;
    private List<WebPageText> selectionForSave = new ArrayList<>();
    private  List<String> strUrl = new ArrayList<>();
    private String checkUrl = "";
    private TextField model = new TextField();
    private Checkbox deepScrapping = new Checkbox();
    private Checkbox ignoreSSL = new Checkbox();
    private TextField textField = new TextField();

    /**
     * Κατασκευαστής κλάσης
     * @param serviceData διαχείριση δεδομένων (Session scope)
     * @param parameters διαχείριση παραμέτρων επισήμανσης (Session scope)
     * @param fastAPIManager διαχείριση του FastAPI server (Application scope)
     * @param pipelineService διαχείριση του pipeline για το Stanford CoreNLP (Application Scope)
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     */
    public UrlsTextAnnotation(ServiceData serviceData, Parameters parameters, FastAPIManager fastAPIManager,
                              PipeLineForStanford pipelineService, HandledProjects handledProjects) {
        this.serviceData = serviceData;
        this.parameters = parameters;
        this.fastAPIManager = fastAPIManager;
        this.pipelineService = pipelineService;
        this.handledProjects = handledProjects;

        setPadding(true);
        setSpacing(true);
        setMargin(true);
        setWrap(false);
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        H2 title = new H2(getTranslation("UrlsTextAnnotation.H2.title")+" " + handledProjects.getProjectName() );

        HorizontalLayout header = setHeader(new HorizontalLayout());

        HorizontalLayout footer = setFooter(new HorizontalLayout());

        SplitLayout body = setBody(new SplitLayout());

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

        createComboBox(urlComboBox, getTranslation("UrlsTextAnnotation.comboUrl.label"), strUrl);

        load = new Button(getTranslation("UrlsTextAnnotation.button.load"), e->{

            checkUrl =  urlComboBox.getValue();
            dataProvider.refreshAll();
            if(checkUrl == null) {
                Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.nullURL"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }
            validURL(checkUrl);

        });

        model.setValue(parameters.getTextModel());
        model.setWidth("200px");
        model.setReadOnly(true);
        model.setLabel(getTranslation("UrlsTextAnnotation.textField.model"));

        deepScrapping.setValue(parameters.isDeepScraping());
        deepScrapping.setReadOnly(false);
        deepScrapping.setLabel(getTranslation("UrlsTextAnnotation.checkBox.deepScraping"));
        deepScrapping.setTooltipText(getTranslation("UrlsTextAnnotation.checkBox.toolTip"));
        deepScrapping.addValueChangeListener(e->{
            boolean value = e.getValue();
            parameters.setDeepScraping(value);
        });

        ignoreSSL.setValue(parameters.isIgnoreSSL());
        ignoreSSL.setReadOnly(false);
        ignoreSSL.setLabel(getTranslation("UrlsTextAnnotation.checkBox.ignoreSSL"));
        ignoreSSL.setTooltipText(getTranslation("UrlsTextAnnotation.checkBox.ignoreSSL.toolTip"));
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
     * "Επισήμανση", όπου ξεκινά η διαδικασία επισήμανσης του επιλεγμένου κειμένου
     * "Προσθήκη ετικέτας", εμφανίζεται ένα παράθυρό διαλόγου όπου ο χρήστης μπορεί να προσθέσει ετικέτες
     * "Αποθήκευση", για την αποθήκευση των επιλεγμένων επισημάνσεων στο αρχείο json
     * @param footer το layout υπο διαμόρφωση
     * @return το διαμορφωμένο layout
     */
    private HorizontalLayout setFooter(HorizontalLayout footer){

        createAnnotation(annotate, getTranslation("UrlsTextAnnotation.button.annotate"));
        createSave(saveChanges, getTranslation("UrlsTextAnnotation.button.save"));

        HorizontalLayout footerLeft= new HorizontalLayout(annotate, addLabelToDictionary(addLabel, getTranslation("UrlsTextAnnotation.button.addLabel")));
        HorizontalLayout footerRight = new HorizontalLayout(saveChanges);
        footerRight.getStyle().set("margin-left", "auto");
        footerRight.setSpacing(true);
        footer.setPadding(true);
        footer.add(footerLeft, footerRight);

        return footer;
    }

    /**
     * Δημιουργείται το κύριο σώμα της σελίδας (SplitLayout).
     * Στο πάνω μέρος περιλαμβάνει έναν Scroller με το πλήρες κείμενο της σελίδας,
     * επιτρέποντας τον χρήστη να επιλέξει ένα τμήμα του με το ποντίκι.
     * Στο κάτω μέρος εμφανίζεται ένας πίνακας με τις επισημάνσεις
     * @param body layout υπο διαμόρφωση
     * @return το διαμορφωμένο layout
     */
    private SplitLayout setBody(SplitLayout body){

        textForAnnotation = new Div();
        textForAnnotation.setWidthFull();
        textForAnnotation.setHeightFull();
        textForAnnotation.setText(".....................");
        textForAnnotation.getStyle().set("user-select" , "text");
        textForAnnotation.getElement().executeJs(
                "this.addEventListener('contextmenu', e => e.preventDefault());"
        );

        Scroller scroller = new Scroller(textForAnnotation);
        scroller.addClassNames(LumoUtility.Border.BOTTOM, LumoUtility.Padding.SMALL);
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

        VerticalLayout verticalLayout_1 = new VerticalLayout(scroller);
        verticalLayout_1.setPadding(true);
        verticalLayout_1.setHeightFull();
        createGrid();
        VerticalLayout verticalLayout_2 = new VerticalLayout(grid);
        verticalLayout_2.setPadding(true);
        verticalLayout_2.setHeightFull();

        body.addToPrimary(verticalLayout_1);
        body.addToSecondary(verticalLayout_2);
        body.setOrientation(SplitLayout.Orientation.VERTICAL);
        body.setSplitterPosition(60);

        body.setSizeFull();

        return body;
    }

    /**
     * Δημιουργία πίνακα με τις επισημάνσεις. Ο πίνακας περιέχει αντικείμενα
     * WebPageText και οι στήλες του περιέχουν τις πληροφορίες id, Διεύθυνση,
     * κείμενο, Επισήμανση και Επισήμανση χρήστη.
     */
    private void createGrid(){
        grid = new Grid<>(WebPageText.class, false);

        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setWidthFull();
        grid.addColumn(BaseAnnotation::getId).setHeader(getTranslation("UrlsTextAnnotation.grid.id")).setComparator(BaseAnnotation::compareTo).setSortable(true).setWidth("50px").setResizable(true);
        grid.addColumn(WebPageText::getUrl).setHeader(getTranslation("UrlsTextAnnotation.grid.url")).setSortable(true).setWidth("100px").setResizable(true);
        grid.addColumn(WebPageText::getText).setHeader(getTranslation("UrlsTextAnnotation.grid.text")).setSortable(true).setWidth("400px").setResizable(true);
        grid.addComponentColumn(item -> {

            return createAnnotateLayout(item, new VerticalLayout());

        }).setHeader(getTranslation("UrlsTextAnnotation.grid.annotation")).setSortable(true).setWidth("400px").setResizable(true);

        grid.addComponentColumn(item -> {
            return createMultiSelectionComboBox(item, new MultiSelectComboBox<>(), serviceData.getStringListWord());

        }).setHeader(getTranslation("UrlsTextAnnotation.grid.userAnnotation")).setWidth("400px").setResizable(true);

        grid.setWidthFull();

        grid.addSelectionListener(selectionEvent -> {
            Set<WebPageText> baseAnnotationSet = grid.getSelectedItems();
            selectionForSave = selectionEvent.getAllSelectedItems().stream().toList();
            if (!baseAnnotationSet.isEmpty()) {
                saveChanges.setEnabled(true);
            } else {
                saveChanges.setEnabled(false);
            }
        });

        dataProvider = new ListDataProvider<>(new ArrayList<>());
        grid.setDataProvider(dataProvider);
        grid.setHeightFull();
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    }

    /**
     * Αρχικοποιεί το ComboBox για την επιλογή διευθύνσεων URL
     * @param comboBox το comboBox που θα δημιουργηθεί
     * @param label ο label του comboBox
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
     * Δημιουργία MultiSelectionComboBox. Στο πεδίο "Επισήμανση χρήστη", ο χρήστης
     * έχει δυνατότητα να επιλέξει ένα πλήθος ετικετών από μια λίστα.
     * @param item αντικείμενο WebPageText
     * @param comboBox το MultiSelectComboBox που θα δημιουργηθεί
     * @param list λίστα με τις ετικέτες
     * @return το MultiSelectComboBox
     */
    private MultiSelectComboBox<String> createMultiSelectionComboBox(BaseAnnotation item, MultiSelectComboBox<String>comboBox, List<String> list){

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
     * Δημιουργεί ένα layout για την εμφάνιση των ετικετών που επέστρεψαν τα μοντέλα
     * μέσα στον πίνακα. Κάθε ετικέτα τοποθετείται μέσα σε ένα Span για να μπορέσει
     * να γίνει αναδίπλωσει του κειμένου.
     * @param item αντικείμενο BaseAnnotation (WebPageText)
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
            if(parameters.isDeepScraping()){
                String deepScrapingText;
                try(PlaywrightDeepScraping deepScraping = new PlaywrightDeepScraping()) {
                    deepScrapingText = deepScraping.getTextPlaywright(url);
                }
                catch (Exception e){
                    deepScrapingText = "errorText";
                }
                if(!deepScrapingText.equals("errorText")) {
                    textForAnnotation.setText(deepScrapingText);
                    urlComboBox.removeClassName("error");
                    urlComboBox.setInvalid(false);
                }
                else{
                    Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.errorExportText"), 3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                }

            }
                else {
                        WebPagePicker webPagePicker = new WebPagePicker(parameters);
                        WebPage webPage = webPagePicker.pickWebPage(url);

                        if (webPage != null && !webPage.getPage().isEmpty()) {

                            textForAnnotation.setText(webPage.getPage());
                            urlComboBox.removeClassName("error");
                            urlComboBox.setInvalid(false);
                        } else {
                            textForAnnotation.setText(getTranslation("UrlsTextAnnotation.Notification.foundText"));
                            Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.Notification.foundText"), 3000, Notification.Position.BOTTOM_START);
                            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                        }
            }
        }
        else{
            urlComboBox.addClassName("error");
            urlComboBox.setInvalid(true);
            urlComboBox.setErrorMessage(getTranslation("UrlsTextAnnotation.comboBox.errorMessage"));
        }
    }

    /**
     * Προσθήκη νέων ετικετών για τις επισημάνσεις που κάνει ο χρήστης. Η προσθήκη γίνεται
     * μέσω ενός παράθυρου διαλόγου. Μετά την προσθήκη ενημερώνεται το αρχείο ετικετών
     *και ανανεώνεται ο πίνακας. Η προσθήκη ετικέτας μπορεί να πραγματοποιηθεί είτε με το
     * Enter είτε με το κουμπί Add
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
            dialog.setHeaderTitle(getTranslation("UrlsTextAnnotation.dialogHeader.title"));

            VerticalLayout layout = new VerticalLayout();

            textField.setReadOnly(false);
           textField.addKeyPressListener(key ->{
              if(key.getKey().matches("Enter")){

                  saveLabelToDictionary();
              }
           });

            layout.add(textField);

            dialog.add(layout);
            Button add = new Button(getTranslation("UrlsTextAnnotation.dialogButton.add"));
            add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            add.addClickListener(click -> {
                saveLabelToDictionary();

            });
            Button close = new Button(getTranslation("UrlsTextAnnotation.dialogButton.close"));
            close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            close.addClickListener(click -> dialog.close());
            dialog.getFooter().add(add);
            dialog.getFooter().add(close);

            dialog.open();

        });

        return btn;
    }

    /**
     * Δημιουργία κουμπιού που εκτελεί την επισήμανση.
     * Γίνεται έλεγχος αν το URL στο ComboBox ταυτίζεται με το κείμενο. Η επισήμανση
     * πραγματοποιείται είτε για επιλεγμένες γραμμές του πίνακα είτε από το κείμενο
     * που έχει επιλέξει ο χρήστης με το ποντίκι.
     *  Αν το μοντέλο επισήμανσης είναι το spaCy και ο FastAPI server δεν απαντήσει
     *  εντός 2 λεπτών, τότε θεωρείται ότι το αίτημα είναι timeout και γίνεται
     *  επανεκκίνηση του server.
     * @param btn κουμπί επισήμανσης
     * @param label του κουμπιού
     */
    private void createAnnotation(Button btn, String label){
        btn.setText(label);
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.setEnabled(true);
        btn.addClickListener( e->{
            if(checkUrl.isBlank()){
                Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.nullURL"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
            else if(!checkUrl.equals(urlComboBox.getValue())){
                Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.errorURL_Text"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
            else if(!grid.getSelectedItems().isEmpty()){
               for(WebPageText text: grid.getSelectedItems()){
                   this.selectedText = text.getText();
                   createAnnotationHelper();

               }
                grid.deselectAll();
            }
            else {
                UI.getCurrent().getPage().executeJs(
                        "return window.getSelection().toString();"
                ).then(String.class, selectedText -> {
                    if (selectedText.isEmpty()) {
                        Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.notSelectText"),3000, Notification.Position.BOTTOM_START);
                        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                    }
                    else {
                        Notification notification =  Notification.show(getTranslation("UrlsTextAnnotation.selectedText")+ " " + selectedText ,3000, Notification.Position.BOTTOM_START);
                        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                        this.selectedText = selectedText;

                        createAnnotationHelper();
                        grid.deselectAll();

                    }
                });
            }
        });

    }

    /**
     * Η μέθοδος createAnnotationHelper είναι βοηθητική και δρομολογεί το κείμενο
     * στο κατάλληλο μοντέλο, ανάλογα με την επιλογή του χρήστη.
     * Στην περίπτωση του spaCy εξετάζεται αν το αίτημα έγινε timeout και αν
     * ο server είναι ζωντανός. Αν ο FastAPI server δεν αποκρίνεται τότε γίνεται
     * επανεκκίνησή του.
     */
    private void createAnnotationHelper(){
        List<String> listAnnotation = new ArrayList<>();
        if(parameters.getStanfordCoreNLP()){
            StanfordCoreAnnotate stanfordCoreNPL = new StanfordCoreAnnotate();
            listAnnotation = stanfordCoreNPL.getStanfordCoreAnnotate(pipelineService.getPipeline(),this.selectedText);
        }
        else if(parameters.getSpacy()){
            Map<String, List<String>> mapAnnotation = new HashMap<>();

            if(fastAPIManager.isServerAlive()) {
                DataForAnnotation dataForAnnotation = new DataForAnnotation(this.selectedText);
                mapAnnotation = fastAPIManager.annotate("TextAnnotation", dataForAnnotation);
            }
            else{

                DataForAnnotation dataForAnnotation = new DataForAnnotation(this.selectedText);
                mapAnnotation = fastAPIManager.annotate("TextAnnotation", dataForAnnotation);
            }
            if(mapAnnotation.containsKey("timeout")){
                Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.timeout"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);

                grid.deselectAll();
                return;
            }
            if(mapAnnotation.containsKey("exception")){
                Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.exception"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                grid.deselectAll();
                return;
            }

            listAnnotation = fastAPIManager.convertData(mapAnnotation);
        }

        WebPageText webPageText = new WebPageText(urlComboBox.getValue(), this.selectedText, listAnnotation, new ArrayList<>(), serviceData);

        dataProvider.getItems().add(webPageText);
        dataProvider.refreshAll();
    }

    /**
     * Δημιουργία του κουμπιού "Αποθήκευση". Η μέθοδος αποθηκεύει τοπικά
     * τα δεδομένα στο αρχείο json.
     * @param btn κουμπί αποθήκευσης
     * @param label label του κουμπιού
     */
    private void createSave(Button btn, String label){

        btn.setText(label);
        btn.addClickListener(e-> {
            if(grid.getSelectedItems().isEmpty()){
                Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.notSelectedRecord"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
            else {

                HandledJsonFile jsonFile = new HandledJsonFile(handledProjects);
                jsonFile.initHandledJsonFile();

                List<BaseAnnotation> base = new ArrayList<>(selectionForSave);

                if (jsonFile.saveBaseAnnotationNew(base)) {
                    Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.saveChanges"),3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    grid.deselectAll();
                } else {
                    Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.errorSaveChanges"),3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    grid.deselectAll();
                }
            }});
        btn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btn.getStyle().set("margin-inline-start", "auto");
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
            Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.dialogNotification.save"),3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            grid.getDataProvider().refreshAll();

        } else {
            Notification notification = Notification.show(getTranslation("UrlsTextAnnotation.dialogNotification.notSave"),3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }
}
