package com.example.application.ui;

import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.uploadData.ServiceData;
import com.example.application.backEnd.handledFiles.UploadSaveData;
import com.example.application.backEnd.uploadData.Url;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Η κλάση UploadUrls διαχειρίζεται το αρχείο των διευθύνσεων του έργου.
 * Ο χρήστης έχει τη δυνατότητα να μεταφορτώσει μια λίστα διευθύνσεων (σε αρχείο txt)
 * Επίσης, μπορεί χειροκίνητα να προσθέσει και να επεξεργαστεί τις διευθύνσεις
 * απευθείας από τον πίνακα και να αποθηκεύσει τις αλλαγές.
 *
 */
@Route(value = UpLoadUrls.ROUTE, layout = MainLayout.class)
public class UpLoadUrls extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "UpLoadUrlsFile";

    private final ServiceData serviceData;
    private final HandledProjects handledProjects;

    private Button addUrl = new Button();
    private Button removeUrl = new Button();
    private Button saveChanges = new Button();
    private Grid<Url> urlsList;
    private ListDataProvider<Url> urlsDataProvider;
    private List<Url> selectedUrls = new ArrayList<>();
    private VerticalLayout horizontalLayout = new VerticalLayout();

    /**
     * Κατασκευαστής κλάσης
     * @param serviceData διαχείριση δεδομένων (Session scope)
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     */
    public UpLoadUrls(ServiceData serviceData, HandledProjects handledProjects){
        this.serviceData = serviceData;
        this.handledProjects = handledProjects;
        if(handledProjects.getProjectName() == null || handledProjects.getProjectName().isBlank()){
            addUrl.setEnabled(false);
            removeUrl.setEnabled(false);
            saveChanges.setEnabled(false);
        }

        addClassName("home-page");

        setPadding(true);
        setSpacing(true);

        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        H2 title = new H2( getTranslation("UploadUrls.H2.title") +  " "+ handledProjects.getProjectName());
        title.addClassName("main-title");

        HorizontalLayout footer = setFooter(new HorizontalLayout());
        footer.setWidthFull();

        SplitLayout body = setBody(new SplitLayout());
        body.setSizeFull();

        add(title, body, footer);

        setFlexGrow(1, body);
    }

    /**
     * Δημιουργία κάτω μέρους της σελίδας όπου γίνεται η διαχείριση των διευθύνσεων.
     * Ο χρήστης μπορεί να εισάγει νέες γραμμές για χειροκίνητη πληκτρολόγηση διευθύνσεων.
     * Μπορεί να διαγράψει επιλεγμένες διευθύνσεις και να αποθηκεύσει τη λίστα.
     * @param footer το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private HorizontalLayout setFooter(HorizontalLayout footer){
        footer.addClassName("view-footer");
        footer.setWidthFull();

        addUrl.setText(getTranslation("UploadUrls.button.add"));
        addUrl.addClickListener( e->{
            List<Url> newLines = new ArrayList<>(urlsDataProvider.getItems().stream().toList());
            newLines.add(new Url(""));
            urlsDataProvider.getItems().clear();
            urlsDataProvider.getItems().addAll(newLines);
            urlsDataProvider.refreshAll();
            urlsList.deselectAll();
        });

        addUrl.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        removeUrl.setText(getTranslation("UploadUrls.button.remove"));
        removeUrl.addClickListener( e->{
            List<Url> newLines = new ArrayList<>(urlsDataProvider.getItems().stream().toList());
            if(!selectedUrls.isEmpty()){
                for(Url url : selectedUrls){
                    newLines.remove(url);
                }
            }

            urlsDataProvider.getItems().clear();
            urlsDataProvider.getItems().addAll(newLines);
            urlsDataProvider.refreshAll();
            urlsList.deselectAll();
        });
        removeUrl.addThemeVariants(ButtonVariant.LUMO_ERROR);

        saveChanges.setText(getTranslation("UploadUrls.button.save"));
        saveChanges.addClickListener( e->{
            List<Url> newLines = new ArrayList<>(urlsDataProvider.getItems().stream().toList());

            UploadSaveData uploadSaveData = new UploadSaveData(handledProjects);
            uploadSaveData.initUploadSaveData();

            if(uploadSaveData.saveUrls(newLines)){
                serviceData.setUrlAddress(newLines);
                Notification notification = Notification.show(getTranslation("UploadUrls.Notification.saveChanges"),3000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            else{
                Notification notification = Notification.show(getTranslation("UploadUrls.Notification.errorSaveChanges"),3000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            urlsList.deselectAll();
        });
        saveChanges.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        HorizontalLayout footerRight = new HorizontalLayout(addUrl, removeUrl, saveChanges);
        footerRight.addClassName("footer-right");
        footerRight.getStyle().set("margin-left", "auto");
        footerRight.setSpacing(true);
        footer.setPadding(true);

        footer.add(footerRight);

        return footer;
    }

    /**
     * Δημιουργία του κυρίου σώματος της σελίδας. Μέσω ενός SplitLayout χωρίζεται
     * το layout σε δύο μέρη. Στο αριστερό τμήμα τοποθετείται η μεταφόρτωση του αρχείου
     * διευθύνσεων και στο δεξιό τμήμα λίστα με τις διευθύνσεις
     * @param body το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private SplitLayout setBody(SplitLayout body) {
        body.addClassName("split-layout");

        createGrid();
        createUpload();
        horizontalLayout.addClassName("upload-card");

        VerticalLayout verticalLayout = new VerticalLayout(urlsList);
        verticalLayout.setSizeFull();
        verticalLayout.setPadding(false);

        body.addToPrimary(horizontalLayout);
        body.addToSecondary(verticalLayout);
        body.setSplitterPosition(30);

        body.setSizeFull();

        return body;
    }

    /**
     * Δημιουργία λίστας με τις διευθύνσεις. Υπάρχει δυνατότητα προσθήκης νέων
     * γραμμών για την εισαγωγή νέων διευθύνσεων. Επίσης, ο χρήστης μπορεί
     * να αφαιρέσει επιλεγμένες γραμμές.
     */
    private void createGrid(){
        urlsList = new Grid<>(Url.class, false);

        if(urlsDataProvider == null || urlsDataProvider.getItems().isEmpty()){
            urlsDataProvider = new ListDataProvider<>(new ArrayList<>());

            List<Url> defaultData = serviceData.getUrlAddress();
            urlsDataProvider.getItems().addAll(defaultData);
            List<Url> defaultUrls = new ArrayList<>();
            defaultUrls.addAll(urlsDataProvider.getItems().stream().toList());
            serviceData.setUrlAddress(defaultUrls);
        }
        else {

            urlsDataProvider = new ListDataProvider<>(new ArrayList<>());
            if (!serviceData.getUrlAddress().isEmpty()) {
                urlsDataProvider.getItems().addAll(serviceData.getUrlAddress());
            }
        }

        urlsList.setSelectionMode(Grid.SelectionMode.MULTI);
        Grid.Column<Url> urlColumn = urlsList.addColumn(Url::getUrl).setHeader(getTranslation("UploadUrls.gridHeaders")).setAutoWidth(true);

        urlsList.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        urlsList.addSelectionListener(selectionEvent -> {
            selectedUrls = selectionEvent.getAllSelectedItems().stream().toList();
            Set<Url> selectedUrlsList = urlsList.getSelectedItems();
            if (!selectedUrlsList.isEmpty() && selectedUrlsList != null) {
                removeUrl.setEnabled(true);
            } else {
                removeUrl.setEnabled(false);
            }
        });

        Editor<Url> editor = urlsList.getEditor();
        Binder<Url> binder = new Binder<>(Url.class);
        editor.setBinder(binder);
        TextField editField = new TextField();
        editField.setWidthFull();

        binder.forField(editField).bind(Url::getUrl, Url::setUrl);

        urlColumn.setEditorComponent(editField);

        urlsList.addItemClickListener(e->{
            editor.editItem(e.getItem());
        });

        urlsList.setDataProvider(urlsDataProvider);
        urlsList.setHeightFull();
        urlsList.setAllRowsVisible(false);
        urlsList.addThemeVariants(GridVariant.LUMO_COMPACT);
        urlsList.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    }

    /**
     * Δημιουργείται ο μηχανισμός μεταφόρτωσης. Χρησιμοποιείται το InMemoryUploadHandler όπου
     * ο χρήστης έχει δυνατότητα είτε να επιλέξει κάποιο αρχείο ή να το σύρει σε
     * μια συγκεκριμένη περιοχή. Τα αποδεκτά αρχεία είναι μόνο txt. Το περιεχόμενου
     * του αρχείου μετατρέπεται σε αντικείμενα Url και εισάγεται στον πίνακα.
     */
    private void createUpload(){
        InMemoryUploadHandler inMemoryUploadHandler = UploadHandler.inMemory(
                (metadata, data) -> {

                    if(!handledProjects.getProjectName().isBlank()) {
                        UploadSaveData uploadSaveData = new UploadSaveData(handledProjects);
                        List<String> urlList = uploadSaveData.loadUrlsWords(data);
                        if (!urlList.isEmpty() && urlList != null) {
                            urlsDataProvider.getItems().clear();
                            for (String str : urlList) {
                                urlsDataProvider.getItems().add(new Url(str));

                            }
                            urlsDataProvider.refreshAll();
                        }
                    }
                });

        Upload upload = new Upload(inMemoryUploadHandler);
        upload.setMaxFiles(1);
        upload.setAcceptedFileTypes("text/plain" , ".txt");
        upload.setSizeFull();
        upload.setAutoUpload(true);
        upload.setDropAllowed(true);

        Paragraph info = new Paragraph(getTranslation("UploadUrls.Notification.info"));

        horizontalLayout.add(info , upload);
        horizontalLayout.setSizeFull();
        horizontalLayout.setPadding(true);
        horizontalLayout.setSpacing(false);
        horizontalLayout.getThemeList().add("spacing-xs");
    }
}
