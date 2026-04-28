package com.example.application.ui;

import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.uploadData.ServiceData;
import com.example.application.backEnd.handledFiles.UploadSaveData;
import com.example.application.backEnd.uploadData.Word;
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
 * Η κλάση UploadWord διαχειρίζεται το αρχείο των ετικετών του έργου.
 * Ο χρήστης έχει τη δυνατότητα να μεταφορτώσει μια λίστα από ετικέτες (σε αρχείο txt)
 * Επίσης, μπορεί χειροκίνητα να προσθέσει και να επεξεργαστεί τις ετικέτες
 * απευθείας από τον πίνακα και να αποθηκεύσει τις αλλαγές.
 */
@Route(value = UploadWord.ROUTE, layout = MainLayout.class)

public class UploadWord extends VerticalLayout {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "UploadWord";

    private final ServiceData serviceData;
    private final HandledProjects handledProjects;

    private Button addWord = new Button();
    private Button removeWord = new Button();
    private Button saveChanges = new Button();

    private Grid<Word> wordsList;
    private ListDataProvider<Word> wordsDataProvider;
    private List<Word> selectedWords = new ArrayList<>();
    private VerticalLayout horizontalLayout = new VerticalLayout();

    /**
     * Κατασκευαστής κλάσης
     * @param serviceData διαχείριση δεδομένων (Session scope)
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     */
    public UploadWord(ServiceData serviceData, HandledProjects handledProjects){
        this.serviceData = serviceData;
        this.handledProjects = handledProjects;

        if(handledProjects.getProjectName() == null || handledProjects.getProjectName().isBlank()){
            addWord.setEnabled(false);
            removeWord.setEnabled(false);
            saveChanges.setEnabled(false);
        }
        addClassName("home-page");

        setPadding(true);
        setSpacing(true);

        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        H2 title = new H2(getTranslation("UploadWord.H2.title")+ " " + handledProjects.getProjectName());
        title.addClassName("main-title");

        HorizontalLayout footer = setFooter(new HorizontalLayout());
        footer.setWidthFull();

        SplitLayout body = setBody(new SplitLayout());
        body.setSizeFull();

        add(title, body, footer);

        setFlexGrow(1, body);
    }

    /**
     * Δημιουργία κάτω μέρους της σελίδας όπου γίνεται η διαχείριση των ετικετών.
     * Ο χρήστης μπορεί να εισάγει νέες γραμμές για χειροκίνητη πληκτρολόγηση ετικετών.
     *  Μπορεί να διαγράψει επιλεγμένες ετικέτες και να αποθηκεύσει τη λίστα.
     * @param footer το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private HorizontalLayout setFooter(HorizontalLayout footer){
        footer.addClassName("view-footer");
        footer.setWidthFull();

        addWord.setText(getTranslation("UploadWord.button.add"));
        addWord.addClickListener( e->{
            List<Word> newLines = new ArrayList<>(wordsDataProvider.getItems().stream().toList());
            newLines.add(new Word(""));
            wordsDataProvider.getItems().clear();
            wordsDataProvider.getItems().addAll(newLines);
            wordsDataProvider.refreshAll();
            wordsList.deselectAll();
        });

        addWord.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        //addWord.getStyle().set("margin-inline-start", "auto");

        removeWord.setText(getTranslation("UploadWord.button.remove"));
        removeWord.addClickListener(e->{
            List<Word> newLines = new ArrayList<>(wordsDataProvider.getItems().stream().toList());
            if(!selectedWords.isEmpty()){
                for(Word word : selectedWords){
                    newLines.remove(word);
                }
            }

            wordsDataProvider.getItems().clear();
            wordsDataProvider.getItems().addAll(newLines);
            wordsDataProvider.refreshAll();
            wordsList.deselectAll();
        });
        removeWord.addThemeVariants(ButtonVariant.LUMO_ERROR);

        saveChanges.setText(getTranslation("UploadWord.button.save"));
        saveChanges.addClickListener( e->{
            List<Word> newLines = new ArrayList<>(wordsDataProvider.getItems().stream().toList());

            UploadSaveData uploadSaveData = new UploadSaveData(handledProjects);
            uploadSaveData.initUploadSaveData();

            if(uploadSaveData.saveWords(newLines)){
                serviceData.setWords(newLines);
                Notification notification = Notification.show(getTranslation("UploadWord.Notification.saveChanges"),3000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            else{
                Notification notification = Notification.show(getTranslation("UploadWord.Notification.errorSaveChanges"),3000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
            wordsList.deselectAll();
        });
        saveChanges.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        HorizontalLayout footerRight = new HorizontalLayout(addWord, removeWord, saveChanges);
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
     * ετικετών και στο δεξιό τμήμα λίστα με τις ετικέτες
     * @param body το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private SplitLayout setBody(SplitLayout body) {
        body.addClassName("split-layout");

        createGrid();
        createUpload();
        horizontalLayout.addClassName("upload-card");

        VerticalLayout verticalLayout = new VerticalLayout(wordsList);
        verticalLayout.setSizeFull();
        verticalLayout.setPadding(false);

        body.addToPrimary(horizontalLayout);
        body.addToSecondary(verticalLayout);
        body.setSplitterPosition(30);

        body.setSizeFull();

        return body;
    }

    /**
     * Δημιουργία λίστας με τις ετικέτες. Υπάρχει δυνατότητα προσθήκης νέων
     * γραμμών για την εισαγωγή νέων ετικετών. Επίσης, ο χρήστης μπορεί
     * να αφαιρέσει επιλεγμένες γραμμές.
     */
    private void createGrid(){
        wordsList = new Grid<>(Word.class, false);

        if(wordsDataProvider == null || wordsDataProvider.getItems().isEmpty()){
            wordsDataProvider = new ListDataProvider<>(new ArrayList<>());

            List<Word> defaultData = serviceData.getWord();
            wordsDataProvider.getItems().addAll(defaultData);
            List<Word> defaultWords = new ArrayList<>();
            defaultWords.addAll(wordsDataProvider.getItems().stream().toList());
            serviceData.setWords(defaultWords);
        }

        wordsDataProvider = new ListDataProvider<>(new ArrayList<>());
        if(!serviceData.getWord().isEmpty()){
            wordsDataProvider.getItems().addAll(serviceData.getWord());
        }

        wordsList.addThemeVariants(GridVariant.AURA_COLUMN_BORDERS, GridVariant.AURA_ROW_STRIPES);
        wordsList.setSelectionMode(Grid.SelectionMode.MULTI);
        Grid.Column<Word> urlColumn = wordsList.addColumn(Word::getWord).setHeader(getTranslation("UploadWord.gridHeaders")).setAutoWidth(true);

        wordsList.addSelectionListener(selectionEvent -> {
            selectedWords = selectionEvent.getAllSelectedItems().stream().toList();
            Set<Word> selectedWordsList = wordsList.getSelectedItems();
            if (!selectedWordsList.isEmpty() && selectedWordsList != null) {

              removeWord.setEnabled(true);
            } else {

                removeWord.setEnabled(false);
            }
        });

        Editor<Word> editor = wordsList.getEditor();
        Binder<Word> binder = new Binder<>(Word.class);
        editor.setBinder(binder);


        TextField editField = new TextField();
        editField.setWidthFull();

        binder.forField(editField).bind(Word::getWord, Word::setWord);

        urlColumn.setEditorComponent(editField);

        wordsList.addItemClickListener(e->{
            editor.editItem(e.getItem());
        });

        wordsList.setDataProvider(wordsDataProvider);
        wordsList.setHeightFull();
        wordsList.setAllRowsVisible(false);
        wordsList.addThemeVariants(GridVariant.LUMO_COMPACT);
        wordsList.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    }

    /**
     * Δημιουργείται ο μηχανισμός μεταφόρτωσης. Χρησιμοποιείται το InMemoryUploadHandler όπου
     *  ο χρήστης έχει δυνατότητα είτε να επιλέξει κάποιο αρχείο ή να το σύρει σε
     * μια συγκεκριμένη περιοχή. Τα αποδεκτά αρχεία είναι μόνο txt. Το περιεχόμενου
     *  του αρχείου μετατρέπεται σε αντικείμενα Word και εισάγεται στον πίνακα.
     */
    private void createUpload(){
        InMemoryUploadHandler inMemoryUploadHandler = UploadHandler.inMemory(
                (metadata, data) -> {
                    if(!handledProjects.getProjectName().isBlank()) {
                        UploadSaveData uploadSaveData = new UploadSaveData(handledProjects);

                        List<String> wordList = uploadSaveData.loadUrlsWords(data);
                        if (!wordList.isEmpty() && wordList != null) {
                            wordsDataProvider.getItems().clear();
                            for (String str : wordList) {
                                wordsDataProvider.getItems().add(new Word(str));
                            }

                            wordsDataProvider.refreshAll();
                        }
                    }
                });

        Upload upload = new Upload(inMemoryUploadHandler);
        upload.setMaxFiles(1);
        upload.setAcceptedFileTypes("text/plain" , ".txt");
        upload.setSizeFull();
        upload.setAutoUpload(true);
        upload.setDropAllowed(true);

        Paragraph info = new Paragraph(getTranslation("UploadWord.Notification.info"));

        horizontalLayout.add(info , upload);
        horizontalLayout.setSizeFull();
        horizontalLayout.setPadding(true);
        horizontalLayout.setSpacing(false);
        horizontalLayout.getThemeList().add("spacing-xs");
    }

}
