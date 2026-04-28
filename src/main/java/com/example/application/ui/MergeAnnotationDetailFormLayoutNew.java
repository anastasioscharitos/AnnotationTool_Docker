package com.example.application.ui;

import com.example.application.backEnd.dataHandling.BaseAnnotation;
import com.example.application.backEnd.handledFiles.HandledProjects;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoIcon;

import java.util.List;
import java.util.stream.Stream;

/**
 * Η κλάση MergeAnnotationDetailFormLayoutNew δημιουργεί FormLayout, δημιουργεί
 * μια φόρμα για την απεικόνιση των επισημάνσεων που έχουν συγχωνευτεί. Η φόρμα
 * διατάσσει τα δεδομένα σε γραμμές, όπου κάθε γραμμή αντιστοιχεί και σε μια επισήμανση.
 * Το μέγεθος της φόρμα είναι 6 στήλες και 2 έως και 4 γραμμές. Οι στήλες περιέχουν τα
 * πεδία εικόνα, id, τύπος επισήμανσης, κείμενο, επισήμανση μοντέλου, επισήμανση χρήστη.
 */
public class MergeAnnotationDetailFormLayoutNew extends FormLayout {
    private final HandledProjects handledProjects;

    /**
     * Κατασκευαστής κλάσης
     * @param handledProjects  διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     * @param size αριθμός στηλών
     */
    public MergeAnnotationDetailFormLayoutNew(HandledProjects handledProjects, int size){

        this.handledProjects = handledProjects;
        setResponsiveSteps(new ResponsiveStep("0", size));

        getElement().getStyle().set("align-items", "stretch");
        getElement().getStyle().set("align-content", "stretch");
        getElement().getStyle().set("--vaadin-form-layout-column-gap", "10px");
        getElement().getStyle().set("--vaadin-form-item-row-spacing", "0");

    }

    /**
     * Κατασκευαστής κλάσης
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     * @param list λίστα με τις εγγραφές που θα συγχωνευτούν
     */
    public MergeAnnotationDetailFormLayoutNew(HandledProjects handledProjects, List<BaseAnnotation> list){

        this.handledProjects = handledProjects;
        setResponsiveSteps(new ResponsiveStep("0", 6));
        setColumnWidth("350px");

        getElement().getStyle().set("align-items", "stretch");
        getElement().getStyle().set("align-content", "stretch");
        getElement().getStyle().set("--vaadin-form-layout-column-gap", "10px");
        getElement().getStyle().set("--vaadin-form-item-row-spacing", "0");

        for(BaseAnnotation baseAnnotation : list){
            Div image = new Div();
            setImage(image, baseAnnotation);
            add(image);

            TextField id = createTextField(getTranslation("MergeAnnotation.grid.id"), baseAnnotation.getId());
            TextField type = createTextField(getTranslation("MergeAnnotation.grid.type"), baseAnnotation.getType());
            TextField text = createTextField(getTranslation("MergeAnnotation.grid.textCaption") , baseAnnotation.getText());
            TextField annotation = createTextField(getTranslation("MergeAnnotation.grid.annotation") , String.join(", " , baseAnnotation.getAnnotation()));
            TextField userAnnotation = createTextField(getTranslation("MergeAnnotation.grid.userAnnotation"), String.join(", " , baseAnnotation.getUserAnnotation()));

            Stream.of(id,type, text, annotation, userAnnotation).forEach(textField -> {
                textField.setReadOnly(true);
                add(textField);
            } );

        }
    }

    /**
     * Η κλάση createTextField δημιουργεί τα πεδία για να εισαχθούν οι ιδιότητες
     * των συγχωνευμένων επισημάνσεων
     * @param label το label κάθε πεδίου
     * @param value η τιμή κάθε πεδίου
     * @return διαμορφωμένο textField (read only)
     */
    private TextField createTextField(String label, String value){
        TextField txt = new TextField(label);
        if(value != null){
            txt.setValue(value);
        }
        else{
            txt.setValue("");
        }
        txt.setReadOnly(true);
        txt.getStyle().set("min-width", "0");
        txt.getElement().getStyle().set("max-width", "100%");
        return txt;
    }

    /**
     * H μέθοδος setImage δημιουργεί ένα Div container και προσθέτει την εικόνα της
     * επισήμανσης. Για την προβολή στον browser και την αποφυγή σφαλμάτων από την
     * ανάγνωση των εικόνων, προστίθεται το /images/ στο μονοπάτι κάθε εικόνας.
     * @param div container Div που θα τοποθετηθεί η εικ΄να ή το εικονίδια (αν η επισήμανση
     *            είναι κείμενο).
     * @param baseAnnotation το αντικείμενο που έχει συγχωνευτεί, περιέχει το μονοπάτι
     *                       της εικόνας
     */
    private void setImage(Div div, BaseAnnotation baseAnnotation){
        div.removeAll();

        div.setHeight("100px");

        div.getStyle().set("display", "flex");
        div.getStyle().set("align-items", "center");
        div.getStyle().set("justify-content", "center");

        div.getStyle().set("line-height", "0");
        div.getStyle().set("vertical-align", "middle");

        String path = baseAnnotation.getImagePath();
        String projectName = handledProjects.getProjectName();

        if(path == null || path.isEmpty()){
            Icon icon = LumoIcon.PHOTO.create();
            icon.setSize("50px");

            icon.getElement().getStyle()
                    .set("display", "block")
                    .set("flex-shrink", "0");

            div.add(icon);
        } else {
            Image img = new Image();
            String newPath = "/images/" +projectName + "/" + path;
            img.setSrc(newPath);
            img.setAlt("img");
            img.getStyle()
                    .set("max-width", "100%")
                    .set("max-height", "100%")
                    .set("object-fit", "contain")
                    .set("display", "block");

            div.add(img);
        }
    }

}

