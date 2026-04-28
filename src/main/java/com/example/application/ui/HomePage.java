package com.example.application.ui;

import com.example.application.backEnd.handledFiles.HandledJsonFile;
import com.example.application.backEnd.handledFiles.HandledLockProject;
import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.uploadData.ServiceData;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.converter.json.GsonBuilderUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Αυτή η κλάση δημιουργεί την κεντρική σελίδα της εφαρμογής.
 * Ο χρήστης έχει τη δυνατότητα:
 * 1) Να δημιουργήσει νέο έργο με την αυτόματη παραγωγή όλων των απαιτούμενων φακέλων (images, metadata)
 * 2) Nα φορτώσει ένα ήδη υπάρχων με έλεγχο του αρχείου json
 * 3) Να διαγράψει ένα έργο.
 * 4) Να διαχειρίζεται το κλείδωμα/ ξεκλείδωμα των έργων έτσι ώστε να μη χρησιμοποιείται
 *      ταυτόχρονα από δύο χρήστες.
 */

@Route(value = HomePage.ROUTE, layout = MainLayout.class)
@RouteAlias(value = "" , layout = MainLayout.class)
public class HomePage extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "HomePage";

    private final HandledProjects handledProjects;
    private final ServiceData serviceData;
    private final HandledLockProject lockProject;

    private TextField nameNewProject;
    private Button load;
    private Button newProject;
    private Button delete;
    private Grid<File> gridFile;
    private String project = "";
    private File projectFile;
    private Button unlockButton;

    /**
     * Κατασκευαστής κλάσης
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     * @param serviceData διαχείριση δεδομένων (Session scope)
     * @param lockProject διαχείριση κλειδώματος (Application scope)
     */
    public HomePage(HandledProjects handledProjects, ServiceData serviceData, HandledLockProject lockProject){
        this.handledProjects = handledProjects;
        this.serviceData = serviceData;
        this.lockProject = lockProject;

        setPadding(true);
        setSpacing(true);
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        H2 title = new H2(getTranslation("HomePage.H2.Start"));

        VerticalLayout createProject = setCreateProject(new VerticalLayout());

        VerticalLayout loadProject = setLoadProject(new VerticalLayout());

        add(title, createProject, loadProject);
        setFlexGrow(1, loadProject);

        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        load.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        unlockButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
        newProject.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        var httpSession = VaadinServletRequest.getCurrent().getHttpServletRequest().getSession();
        System.out.println("Timeout VAADIN: " + httpSession.getMaxInactiveInterval());

    }

    /**
     * Σε αυτήν την κλάση δημιουργείται ένα layout το οποίο ο χρήστης μπορεί να δημιουργήσει
     * ένα νέο έργο. Το όνομα του έργου πρέπει να περιέχει μόνο τους χαρακτήρες a-z α-ω A-Z Α-Ω 0-9 ._
     * Αν δεν μπορεί να δημιουργηθεί το έργο εμφανίζεται μήνυμα στην οθόνη.
     * @param layout το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private VerticalLayout setCreateProject(VerticalLayout layout){
        H4 title = new H4(getTranslation("HomaPage.H4.NewProject"));

        nameNewProject = new TextField();
        nameNewProject.setReadOnly(false);
        nameNewProject.setAllowedCharPattern("[a-z α-ω A-Z Α-Ω 0-9 . _]");
        nameNewProject.setMaxLength(20);
        nameNewProject.setLabel(getTranslation("HomePage.textField.nameProject"));

        newProject = new Button(getTranslation("HomePage.button.newProject"));
        newProject.addClickListener( e->{
                project = nameNewProject.getValue();
                LocalDate creationDate = LocalDate.now();
                if(handledProjects.getProjectName() != null){
                    lockProject.unlockProject(handledProjects.getProjectName());
                }

                if(checkName(project) && handledProjects.createProject(project, creationDate)){

                    if(serviceData.initProjectData()){

                        lockProject.lockProject(project);

                        Notification notification = Notification.show(getTranslation("HomePage.Notification.SuccessProject"), 3000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
                        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                        VaadinSession.getCurrent().setAttribute("LOCKED_PROJECT", project);
                        List<File> fileList = handledProjects.getAllProjectNames();

                        if (fileList == null) {
                            fileList = new ArrayList<>();
                        }
                        gridFile.setItems(fileList);
                        nameNewProject.setValue("");
                    }
                    else{
                        Notification notification = Notification.show(getTranslation("HomePage.Notification.ErrorProject"), 3000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        project = "";
                        handledProjects.clearProject();

                        serviceData.initProjectData();
                    }
                }
                else{
                    Notification notification = Notification.show(getTranslation("HomePage.Notification.ErrorProject"), 3000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    project = "";
                    handledProjects.clearProject();

                    serviceData.initProjectData();
                }
        });

        layout.add(title, nameNewProject, newProject);

        return layout;
    }

    /**
     * Η μέθοδος ελέγχει αν το όνομα ενός project χρησιμοποιείται σε άλλο project.
     * @param value String το προτεινόμενο όνομα του νέου project
     * @return true αν δε χρησιμοποιείται το όνομα και false διαφορετικά
     */
    private boolean checkName(String value){
        if(value.isBlank()){
            return false;
        }
        String projectName = value + ".json";
        List<File> fileList = handledProjects.getAllProjectNames();

        if(fileList == null || fileList.isEmpty()){
            return true;
        }

        for(File file : fileList){
            if(file.getName().equals(projectName)){
                return false;
            }
        }
        return true;
    }

    /**
     * Η μέθοδος δημιουργεί το layout για τη φόρτωση των project. Δημιουργεί δύο κουμπιά
     * load και delete τα οποία φορτώνουν και διαγράφουν ένα project, αντίστοιχα.
     * Όλα τα διαθέσιμα project εμφανίζονται σε έναν πίνακα. Όταν επιλέγεται ένα project
     * επιστρέφεται μήνυμα επιτυχίας αν φορτώθηκε επιτυχώς ή σφάλματος σε περίπτωση
     * αποτυχίας. Στην περίπτωση της διαγραφή ανοίγει ένα παράθυρο διαλόγου.
     * @param layout το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private VerticalLayout setLoadProject(VerticalLayout layout){

        load = new Button(getTranslation("HomePage.button.load"));
        load.setEnabled(false);
        load.addClickListener(e->{

            if(lockProject.isLockedProject(project)){
                Notification notification = Notification.show(getTranslation("HomePage.Notification.LockProject"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
            else {
                if (handledProjects.getProjectName() != null) {
                    lockProject.unlockProject(handledProjects.getProjectName());
                }
                handledProjects.loadProject(project);
                if (serviceData.initProjectData()) {
                    try {

                        HandledJsonFile json = new HandledJsonFile(handledProjects);
                        json.initHandledJsonFile();
                        json.loadJsonFile();
                        lockProject.lockProject(project);
                        Notification notification = Notification.show(getTranslation("HomePage.Notification.SuccessLoadProject"), 3000, Notification.Position.BOTTOM_START);
                        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        VaadinSession.getCurrent().setAttribute("LOCKED_PROJECT", project);
                    }
                    catch (Exception exception) {
                        Notification notification = Notification.show( getTranslation("HomePage.Notification.ErrorLoadProject")+ " " + project, 3000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        project = "";
                        handledProjects.clearProject();

                        serviceData.initProjectData();
                    }

                } else {
                    Notification notification = Notification.show(getTranslation("HomePage.Notification.ErrorIDCounterTxt"), 3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    project = "";
                    handledProjects.clearProject();

                    serviceData.initProjectData();
                }
            }
        });

        delete = new Button(getTranslation("HomePage.button.delete"));
        delete.setEnabled(false);
        delete.addClickListener( e->{
            deleteDialog(new Dialog()).open();
        });

        unlockButton = new Button(getTranslation("HomePage.button.unlock"));

        unlockButton.addClickListener(e->{
            String filename = handledProjects.getProjectName();
            if(!gridFile.getSelectedItems().isEmpty()){
                File file = gridFile.getSelectedItems().stream().findAny().get();
                filename = file.getName().split("\\.")[0];
                System.out.println(filename);
                System.out.println(handledProjects.getProjectName());
            }

            if(!handledProjects.getProjectName().isBlank() && handledProjects.getProjectName().equals(filename)){

                Notification notification = Notification.show( getTranslation("HomePage.button.unlockNotification")+ " " + handledProjects.getProjectName(), 3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                lockProject.unlockProject(handledProjects.getProjectName());
                project = "";
                handledProjects.clearProject();

                serviceData.initProjectData();
            }
            else{
                Notification notification = Notification.show(getTranslation("HomePage.button.unlockNotificationNoProject"), 3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });


        H4 title = new H4(getTranslation("HomePage.H4.loadProject"));
        List<File> fileList = handledProjects.getAllProjectNames();

        if(fileList == null){
            fileList = new ArrayList<>();
        }

        createGrid(fileList);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.add(load, delete , unlockButton);

        layout.add(title, gridFile, buttonLayout);

        return layout;
    }

    /**
     * Η μέθοδος δημιουργεί τον πίνακα με τα project που υπάρχουν. Στον πίνακα εμφανίζονται
     * μεταδεδομένα όπως το όνομα του project, ημερομηνία δημιουργίας, τροποποίησης και μέγεθος σε KB.
     * @param fileList Λίστα με File που αντιστοιχούν σε έργα.
     */
    private void createGrid(List<File> fileList){
        gridFile = new Grid<>(File.class, false);
        gridFile.setItems(fileList);
        project = "";

        gridFile.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        gridFile.setEmptyStateText(getTranslation("HomePage.grid.Notification"));
        gridFile.addColumn(File::getName).setHeader(getTranslation("HomePage.grid.nameProject")).setSortable(true);
        gridFile.addColumn(file -> {
            LocalDate creationDate = handledProjects.loadCreationDate(file);
            if(creationDate !=  null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return creationDate.format(formatter);
            }
            else{
                return "";
            }
        }).setSortable(true).setHeader(getTranslation("HomePage.grid.dateCreated"));
        gridFile.addColumn(file ->
                new SimpleDateFormat("dd/MM/yyy").format(new Date(file.lastModified()))
        ).setSortable(true).setHeader(getTranslation("HomePage.grid.dateModified"));

        gridFile.addColumn(file-> handledProjects.getSizeProject_KB(file) + " KB").setSortable(true).setHeader("Μέγεθος");

        gridFile.addSelectionListener(e->{

            Optional<File> selectedItem = e.getFirstSelectedItem();
            if (selectedItem.isPresent()) {
                load.setEnabled(true);
                delete.setEnabled(true);

                projectFile = selectedItem.get();

                project =projectFile.getName().split("[.]")[0];

            }
            else{
                load.setEnabled(false);
                delete.setEnabled(false);
            }

        });

        gridFile.setHeightFull();
        gridFile.setAllRowsVisible(true);
        gridFile.addThemeVariants(GridVariant.LUMO_COMPACT);
        gridFile.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    }

    /**
     * Παράθυρο διαλόγου για να επιβεβαιώσει ότι ο χρήστης θέλει να διαγράψει το επιλεγμένο έργο.
     * Η διαγραφή πραγματοποιείται εφόσον το έργο δεν είναι κλειδωμένο
     * @param deleteDialog το παράθυρο διαλόγου που θα δημιουργηθεί
     * @return παράθυρο διαλόγου.
     */
    private Dialog deleteDialog(Dialog deleteDialog){
        deleteDialog.setDraggable(true);
        deleteDialog.setCloseOnEsc(true);
        deleteDialog.setHeaderTitle(getTranslation("HomePage.dialog.titleDelete"));

        VerticalLayout layout = new VerticalLayout();

        Span message = new Span();
        message.setText(getTranslation("HomePage.dialog.message") +  " " + projectFile.getName());

        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setWidth("400px");

        layout.add(message);

        deleteDialog.add(layout);

        Button  delete = new Button(getTranslation("HomePage.dialog.buttonDelete"));
        delete.addClickListener(e-> {
            if(lockProject.isLockedProject(project)){
                Notification notification = Notification.show(getTranslation("HomePage.Notification.LockProject"),3000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
            else {

                if (handledProjects.deleteProject(projectFile.getParentFile())) {
                    List<File> fileList = handledProjects.getAllProjectNames();
                    String deleteProject = project;

                    project = "";
                    try {
                        handledProjects.loadProject(project);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    serviceData.initProjectData();

                    if (fileList == null) {
                        fileList = new ArrayList<>();
                    }
                    gridFile.setItems(fileList);
                    Notification notification = Notification.show(getTranslation("HomePage.dialog.Notification.successDelete") + " " + deleteProject, 3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                else {
                    Notification notification = Notification.show(getTranslation("HomePage.dialog.Notification.errorDelete"), 3000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }

            deleteDialog.close();
        });

        Button cancelButton = new Button(getTranslation("HomePage.dialog.button.cancelButton"), e -> deleteDialog.close());
        deleteDialog.getFooter().add(delete);
        deleteDialog.getFooter().add(cancelButton);

        return deleteDialog;
    }

}