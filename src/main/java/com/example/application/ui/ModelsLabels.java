package com.example.application.ui;

import com.example.application.backEnd.settings.LabelsSpaCy;
import com.example.application.backEnd.settings.LabelsStanfordCoreNLP;
import com.example.application.backEnd.settings.LabelsYolo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.Serial;
import java.util.List;
import java.util.Set;

/**
 *Η κλάση ModelsLabels είναι υπεύθυνη για την εμφάνιση όλων των labels που χρησιμοποιούν
 * τα μοντέλα Yolo, spaCy και Stanford CoreNLP.
 */
@Route(value = ModelsLabels.ROUTE, layout = MainLayout.class)
public class ModelsLabels extends VerticalLayout{

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "Models labels";

    /**
     * Κατασκευαστής κλάσης
     */
    public ModelsLabels(){

        setPadding(true);
        setSpacing(true);

        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        H2 title = new H2(getTranslation("ModelsLabels.title"));
        title.getStyle().set("margin", "20px");

        VerticalLayout body = setBody(new VerticalLayout());
        body.setSizeFull();
        add(title, body);
        setFlexGrow(1,body);
    }

    /**
     * Τοποθετεί τις ετικέτες κάθε μοντέλου σε οργανωμένα container
     * @param body το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private VerticalLayout setBody(VerticalLayout body) {

        List<String> stanfordList = LabelsStanfordCoreNLP.getKeys();
        List<String> yoloList = LabelsYolo.getKeys();
        List<String> spaCyList = LabelsSpaCy.getTags();

        body.add(createTitle(getTranslation("ModelsLabels.spacy")));
        body.add(createLabelsContainer(spaCyList));

        body.add(createTitle(getTranslation("ModelsLabels.stanford")));
        body.add(createLabelsContainer(stanfordList));

        body.add(createTitle(getTranslation("ModelsLabels.yolo")));
        body.add(createLabelsContainer(yoloList));

        return body;
    }

    /**
     * Δημιουργία τον τίτλου Η4 για την ενότητα κάθε μοντέλου
     * @param txt τίτλος
     * @return επιστέφει τον τίτλο
     */
    private H4 createTitle(String txt){
        H4 title = new H4(txt);
        return  title;
    }

    /**
     * Δημιουργία ενός FlexLayout για την εμφάνιση των labels. Για κάθε label
     * δημιουργείται ένα span, το οποίο προστίθεται στο layout. Η χρήση FlexWrap
     * εξασφαλίζει ότι οι ετικέτες θα μεταφέρονται στην επόμενη σειρά αν το πλάτος της οθόνης
     * δεν επαρκεί.
     * @param list λίστα με τα labels κάθε μοντέλου
     * @return το διαμορφωμένο layout
     */
    private FlexLayout createLabelsContainer(List<String> list){
        FlexLayout flexLayout = new FlexLayout();
        flexLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        for(String str: list){
            Span badge = new Span(str);
            badge.getElement().getThemeList().add("badge pill");
            flexLayout.add(badge);
           // applyStyle(flexLayout);

        }
        return flexLayout;
    }

    /**
     * Εφαρμόζει κοινό style (border, background, shadow) στο container των ετικετών
     * @param layout το προς διαμόρφωση layout
     */
    private void applyStyle(Component layout){
        layout.getStyle().set("border", "1px solid #e3e3e3");
        layout.getStyle().set("border-radius", "8px");
        layout.getStyle().set("background-color", "#5E6D97");
        layout.getStyle().set("padding", "15px");
        layout.getStyle().set("box-shadow", "--lumo-box-shadow-s");
    }


}
