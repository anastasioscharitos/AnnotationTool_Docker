package com.example.application.ui;


import com.example.application.backEnd.dataHandling.*;
import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.handledFiles.HandledJsonFile;
import com.example.application.backEnd.uploadData.ServiceData;
import com.example.application.backEnd.handledFiles.UploadSaveData;
import com.example.application.backEnd.uploadData.Word;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Αυτή η κλάση είναι υπεύθυνη για την εμφάνιση όλων των επισημάνσεων που έχουν
 * αποθηκευτεί στο αρχείο json.
 * Οι ενέργειες που εκτελούνται είναι:
 * 1) Συγχώνευση 2 έως και 4 επισημάνσεων σε μια νέα εγγραφή
 * 2) Διαγραφή και ενημέρωση επιλεγμένων εγγραφών
 * 3) Εμφάνιση των επισημάνσεων συγχώνευσης.
 */
@Route(value =  LoadData.ROUTE , layout = MainLayout.class)
public class LoadData extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "Load_Data";

    private Button save;
    private Button delete;
    private Button merge;
    private Button addLabel = new Button();
    private List<BaseAnnotation> selectionForSave = new ArrayList<>();
    private Grid<BaseAnnotation> grid;
    private ListDataProvider<BaseAnnotation> dataProvider = new ListDataProvider<>(new ArrayList<>());
    private final ServiceData serviceData;
    private final HandledProjects handledProjects;
    private TextField textField = new TextField();


    /**
     * Κατασκευαστής κλάσης. Στην περίπτωση κατά την οποία παρουσιαστεί σφάλμα
     * κατά τη φόρτωση των δεδομένων εμφανίζεται μήνυμα σφάλματος
     * @param serviceData διαχείριση δεδομένων (Session scope)
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     */
    public LoadData(ServiceData serviceData, HandledProjects handledProjects) {
        this.serviceData = serviceData;
        this.handledProjects = handledProjects;

        if(!handledProjects.getProjectName().isEmpty()) {
            HandledJsonFile handledJsonFile = new HandledJsonFile(handledProjects);
            handledJsonFile.initHandledJsonFile();
            AnnotatedData annotatedData = new AnnotatedData();
            try {
                List<AnnotatedData> annotatedDataList = handledJsonFile.loadJsonFile();
                List<BaseAnnotation> baseAnnotationList = annotatedData.convertDataToBase(annotatedDataList);

                dataProvider.getItems().clear();
                dataProvider.getItems().addAll(baseAnnotationList);
                dataProvider.refreshAll();
            } catch (Exception e) {
                dataProvider.getItems().clear();
                dataProvider.refreshAll();
                Notification notification =  Notification.show(getTranslation("LoadData.errorLoadData"), 3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }

        setPadding(true);
        setSpacing(true);
        setMargin(true);
        setWrap(false);
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        H2 title = new H2(getTranslation("LoadData.H2.title")+ " " + handledProjects.getProjectName());

        HorizontalLayout footer = setFooter(new HorizontalLayout());

        HorizontalLayout body = setBody(new HorizontalLayout());

        add(title, body, footer);
    }

    /**
     * Διαμορφώνει το κύριο σώμα της σελίδας που περιέχει τον πίνακας με όλες τις επισημάνσεις
     * @param body  layout προς διαμόρφωση
     * @return ο διαμορφωμένο layout
     */
    private HorizontalLayout setBody(HorizontalLayout body) {
        grid = creteGrid();
        VerticalLayout verticalLayout = new VerticalLayout(grid);

        body.add(verticalLayout);
        body.setSizeFull();

        return body;
    }

    /**
     * Δημιουργείται το κάτω μέρος της σελίδας με τα κουμπιά ελέγχου:
     * "Συγχώνευση", μπορούν να συγχωνευτούν από δύο έως και τέσσερις επισημάνσεις
     * "Αποθήκευση", αποθήκευση επιλεγμένων επισημάνσεων στο αρχείο json
     * "Διαγραφή", διαγραφή επιλεγμένων επισημάνσεων
     * @param footer ο layout προς διαμόρφωση
     * @return το διαμορφωμένο layout
     */
    private HorizontalLayout setFooter(HorizontalLayout footer) {
        HorizontalLayout footerLeft = new HorizontalLayout(addLabelToDictionary(addLabel, getTranslation("LoadData.dialogTitle.label")));

        HorizontalLayout footerRight = new HorizontalLayout();
        merge = new Button(getTranslation("LoadData.button.merge"), e -> {
            createMergeItem();

        });
        merge.getStyle().set("margin-inline-start", "auto");
        merge.setEnabled(false);

        save = new Button(getTranslation("LoadData.button.save"), e -> {
            createSave();
        });
        save.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        save.getStyle().set("margin-inline-start", "auto");

        delete = new Button(getTranslation("LoadData.button.delete"), e -> {
                createDelete();
        });

        delete.addThemeVariants(ButtonVariant.LUMO_WARNING);
        delete.getStyle().set("margin-inline-start", "auto");

        footerRight.setPadding(true);
        footerRight.getStyle().set("margin-left", "auto");
        footerRight.add(merge, save, delete);

        footer.add(footerLeft, footerRight);

        return footer;
    }

    /**
     * Εκτελείται η διαδικασία της συγχώνευσης από 2 έως και 4 επιλεγμένες εγγραφές (BaseAnnotation).
     * Δημιουργείται νέο αντικείμενο WebPageMergeNew και προστίθεται στον πίνακα
     */
    private void createMergeItem(){
        List<BaseAnnotation> newLines = new ArrayList<>(dataProvider.getItems().stream().toList());
        if (selectionForSave.size() >= 2 && selectionForSave.size() <=4) {

            WebPageMergeNew mergeAnnotation = new WebPageMergeNew(selectionForSave, serviceData);
            newLines.add(mergeAnnotation);

            grid.deselectAll();
            dataProvider.getItems().clear();
            dataProvider.getItems().addAll(newLines);
            dataProvider.refreshAll();
        }
    }

    /**
     * Αποθήκευση των επιλεγμένων εγγραφών. Η αποθήκευση γίνεται με τη μέθοδο
     * saveBaseAnnotationNew (κλάση HandledJsonFile) όπου φορτώνεται το αρχείο json και γίνεται η αποθήκευση
     * των επιλεγμένων εγγραφών. Αν μια εγγραφή υπάρχει ήδη στο αρχείο τότε αυτή αφαιρείται
     * και αποθηκεύεται η νέα.
     */
    private void createSave(){

        HandledJsonFile jsonFile = new HandledJsonFile(handledProjects);
        jsonFile.initHandledJsonFile();
        List<BaseAnnotation> list = grid.getListDataView().getItems().toList();
        if(jsonFile.saveBaseAnnotationNew(list)) {
            grid.deselectAll();
            Notification notification = Notification.show(getTranslation("LoadData.Notification.saveChanges"), 3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        else{
            grid.deselectAll();
            Notification notification = Notification.show(getTranslation("LoadData.Notification.errorSaveChanges"), 3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Δημιουργία διαγραφής επιλεγμένων εγγραφών.  Η διαγραφή εκτελείται με τη
     * μέθοδο deleteBaseAnnotation (κλάση HandledJsonFile) όπου φορτώνεται το json αρχείο
     * και αφαιρούνται οι επιλεγμένες εγγραφές.
     */
    private void createDelete(){

        Set<BaseAnnotation> baseAnnotationSet = grid.getSelectedItems();

        ListDataProvider<BaseAnnotation> listForDelete = (ListDataProvider<BaseAnnotation>) grid.getDataProvider();

        listForDelete.getItems().removeAll(baseAnnotationSet);
        listForDelete.refreshAll();

        HandledJsonFile jsonFile = new HandledJsonFile(handledProjects);
        jsonFile.initHandledJsonFile();

        List<BaseAnnotation> list = baseAnnotationSet.stream().toList();
        if(jsonFile.deleteBaseAnnotation(list)) {
            Notification notification = Notification.show(getTranslation("LoadData.Notification.deleteRecord"), 3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        else{
            Notification notification = Notification.show(getTranslation("LoadData.Notification.errorDeleteRecord"), 3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Δημιουργία πίνακα (στοιχεία BaseAnnotation) με τις επισημάνσεις. Περιλαμβάνει
     * Component Renderers για εικόνες, επισημάνσεις και πτυσσόμενα τμήματα (ItemDetails)
     * για την προβολή των στοιχείων συγχώνευσης.
     * @return πίνακα με αντικείμενα BaseAnnotation
     */
    private Grid<BaseAnnotation> creteGrid() {
        Grid<BaseAnnotation> grid = new Grid<>(BaseAnnotation.class, false);

        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setColumnReorderingAllowed(true);
        grid.setEmptyStateText(getTranslation( "LoadData.grid.Notification.noRecords"));
        grid.addColumn(BaseAnnotation::getId).setHeader(getTranslation( "LoadData.grid.id")).setComparator(BaseAnnotation::compareTo).setSortable(true).setWidth("100px").setResizable(true);
        grid.addColumn(BaseAnnotation::getType).setHeader(getTranslation( "LoadData.grid.type")).setSortable(true).setWidth("150px").setResizable(true);
        grid.addColumn(BaseAnnotation::getUrl).setHeader(getTranslation( "LoadData.grid.url")).setSortable(true).setWidth("150px").setWidth("150px").setResizable(true);

        grid.addComponentColumn(e -> {
            return zoomImage(e);
        }).setHeader(getTranslation( "LoadData.grid.image")).setAutoWidth(false).setWidth("150px").setResizable(true);

        grid.addColumn(BaseAnnotation::getText).setHeader(getTranslation( "LoadData.grid.textCaption")).setSortable(true).setWidth("150px").setResizable(true);

        grid.addComponentColumn(item -> {

            return createAnnotateLayout(item, new VerticalLayout());

        }).setHeader(getTranslation( "LoadData.grid.annotation")).setSortable(true).setWidth("400px").setResizable(true);


        grid.addComponentColumn(item -> {
                return createMultiSelectionComboBox(item, new MultiSelectComboBox<>(), serviceData.getStringListWord());
        }).setHeader(getTranslation( "LoadData.grid.userAnnotation")).setWidth("400px").setResizable(true);

        grid.addSelectionListener(selectionEvent -> {
            createSelectionListener();

        });

        grid.setDataProvider(dataProvider);
        grid.setHeightFull();
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        grid.setItemDetailsRenderer(new ComponentRenderer<MergeAnnotationDetailFormLayoutNew, BaseAnnotation>(item -> {
                return createRender(item);

        }));


        return grid;

    }

    /**
     * Η μέθοδος createRender δημιουργεί μια φόρμα για την εμφάνιση των συγχωνευμένων
     * επισημάνσεων από ένα αντικείμενο WebPageMergeNew. Για την εμφάνιση του
     * δημιουργείται αντικείμενο MergeAnnotationDetailFormLayoutNew το οποίο
     * περιέχει όλες τις επισημάνσεις.
     * @param item αντικείμενα WebPageMergeNew
     * @return αντικείμενα MergeAnnotationDetailFormLayoutNew
     */
    private MergeAnnotationDetailFormLayoutNew createRender(BaseAnnotation item){
        if (item instanceof WebPageMergeNew mergeItem) {

            int size = selectionForSave.size();
            List<BaseAnnotation> items = mergeItem.getBaseAnnotationList();

            MergeAnnotationDetailFormLayoutNew details = new MergeAnnotationDetailFormLayoutNew(handledProjects, items);
            details.getStyle().setBorder("5px solid #FF282D");

            return details;
        }
        return null;
    }

    /**
     * Δημιουργία SelectionListener για τον πίνακα. Διαχειρίζεται την κατάσταση των
     * κουμπιών ανάλογα με το πλήθος των επιλεγμένων εγγραφών.
     */
    private void createSelectionListener(){
        Set<BaseAnnotation> baseAnnotationSet = grid.getSelectedItems();
        if (!baseAnnotationSet.isEmpty()) {
            save.setEnabled(true);
            delete.setEnabled(true);

        } else {
            save.setEnabled(false);
            delete.setEnabled(false);
        }

        if (baseAnnotationSet.size() >= 2 && baseAnnotationSet.size() <= 4) {
            merge.setEnabled(true);
            selectionForSave.clear();
            selectionForSave.addAll(baseAnnotationSet);
        } else {
            merge.setEnabled(false);
        }

    }
    /**
     * Δημιουργία MultiSelectionComboBox. Στο πεδίο "Επισήμανση χρήστη", ο χρήστης
     * έχει δυνατότητα να επιλέξει ένα πλήθος ετικετών από μια λίστα.
     * @param item αντικείμενο BaseAnnotation
     * @param comboBox το MultiSelectComboBox που θα δημιουργηθεί
     * @param list λίστα με τις ετικέτες
     * @return το MultiSelectComboBox
     */
    private MultiSelectComboBox<String> createMultiSelectionComboBox(BaseAnnotation item,  MultiSelectComboBox<String>comboBox, List<String> list){

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
     * Δημιουργία Span την εμφάνιση επισήμανση στον πίνακα.
     * @param item αντικείμενο BaseAnnotation
     * @param layout το υπo διαμόρφωση layout στο οποίο θα προστεθούν τα span
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
     * Διαχειρίζεται την προβολή και τη μεγέθυνση της εικόνας μέσω ενός παράθυρου διαλόγου,
     * όταν ο χρήστης κάνει click. Αν η εικόνα δεν ξεκινά με http τότε τροποποιείται το μονοπάτι
     * προσθέτοντας /images/. Αυτό επιτρέπει τον browser να παρακάμψει περιορισμούς ασφαλείας για
     *  απευθείας πρόσβαση στο σύστημα αρχείων.
     *  @param item αντικείμενο BaseAnnotation
     *  @return εικόνα σε μεγέθυνση
     */
    private Component zoomImage(BaseAnnotation  item){
        Image image = new Image();
        if(item.getImagePath().equals("")){
            Icon icon = LumoIcon.PHOTO.create();
            return icon;
        }

        String projectName = handledProjects.getProjectName();
        String filename = item.getImagePath();

        String url = "/images/" + projectName + "/" + filename;

        image.setSrc(url);
        image.setAlt("img");

        image.getStyle().set("max-width" , "100%");
        image.getStyle().set("max-height" , "100%");
        image.getStyle().set("object-fit" , "contain");

        image.addClickListener(img->{
            Dialog dialog = new Dialog();
            dialog.setCloseOnEsc(true);
            dialog.setCloseOnOutsideClick(true);
            dialog.setDraggable(true);
            String projectName_ = handledProjects.getProjectName();
            String filename_ = item.getImagePath();

            String url_ = "/images/" + projectName_ + "/" + filename_;

            Image  zoomImg = new Image(url_, "Full view");
            zoomImg.setWidth("600px");

            zoomImg.addDoubleClickListener(click -> dialog.close());
            dialog.add(zoomImg);
            dialog.open();

            Notification.show(getTranslation("LoadData.Notification.selectImage"),3000, Notification.Position.BOTTOM_START);});
        return image;
    }

    /**
     * Προσθήκη νέων ετικετών για τις επισημάνσεις που κάνει ο χρήστης. Η προσθήκη γίνεται
     * μέσω ενός παραθύρου διαλόγου. Μετά την προσθήκη ενημερώνεται το αρχείο ετικετών
     * και ανανεώνεται ο πίνακας.
     * @param btn το κουμπί που θα διαμορφωθεί
     * @param label η νέα ετικέτα
     * @return το κουμπί
     */
    private Button addLabelToDictionary(Button btn, String label){
        btn.setText(label);
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.addClickListener(e-> {
            Dialog dialog = new Dialog();
            dialog.setDraggable(true);
            dialog.setCloseOnEsc(true);
            dialog.setHeaderTitle(getTranslation("LoadData.dialogHeader.title"));

            VerticalLayout layout = new VerticalLayout();

            //TextField textField = new TextField();

            textField.setReadOnly(false);
            textField.addKeyPressListener(key->{
                if(key.getKey().matches("Enter")){
                    saveLabelToDictionary();
                }
            });
            layout.add(textField);

            dialog.add(layout);
            Button add = new Button(getTranslation( "LoadData.dialogButton.add"));
            add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            add.addClickListener(click -> {
                saveLabelToDictionary();

            });
            Button close = new Button(getTranslation( "LoadData.dialogButton.close"));
            close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            close.addClickListener(click -> dialog.close());
            dialog.getFooter().add(add);
            dialog.getFooter().add(close);

            dialog.open();

        });

        return btn;
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
            Notification notification = Notification.show(getTranslation("LoadData.dialogNotification.save"),3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            grid.getDataProvider().refreshAll();

        } else {
            Notification notification = Notification.show(getTranslation("LoadData.dialogNotification.notSave"),3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }
}
