package com.example.application.ui;


import com.example.application.backEnd.annotation.PipeLineForStanford;
import com.example.application.backEnd.annotation.FastAPIManager;
import com.example.application.backEnd.settings.Parameters;
import com.vaadin.copilot.shaded.checkerframework.checker.units.qual.N;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.addons.componentfactory.PaperSlider;

import java.io.Serial;
import java.util.*;

/**
 * Στην κλάση Settings ο χρήστης μπορεί να διαμορφώσει τη συμπεριφορά της
 * εφαρμογής και των μοντέλων επισήμανσης.
 * Οι ρυθμίσεις χωρίζονται σε τέσσερα πεδία:
 * α. Γενικές ρυθμίσεις. Ρύθμιση γλώσσας (ελληνικά/αγγλικά), μέθοδος scraping και ασφάλεια SSL
 * β. Παράμετροι μοντέλων. Ρύθμιση topK (αριθμός αποτελεσμάτων) και threshold (κατώφλι)
 * γ. Μοντέλα κειμένου. Επιλογή μεταξύ spaCy και Stanford CoreNLP.
 * δ. Μοντέλα εικόνας. Επιλογή μεταξύ YOLO, YOLO World και CLIP
 */
@Route(value = Settings.ROUTE, layout = MainLayout.class)
public class Settings extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "Settings";

    private final Parameters parameters;
    private final PipeLineForStanford pipelineService;
    private final FastAPIManager fastAPIManager;


    private ComboBox<String> topK = new ComboBox<>();
    private PaperSlider threshold;
    private RadioButtonGroup<String> spacy = new RadioButtonGroup<>();
    private RadioButtonGroup<String> yolo = new RadioButtonGroup<>();

    private List<String> setLabelSpacy;
    private List<String> setLabelYolo;
    private Button save;
    private Button defaultButton;
    private ComboBox<String> selectLanguage = new ComboBox<>();
    private Checkbox selectScraping= new Checkbox();
    private Checkbox ignoreSSL = new Checkbox();

    /**
     * Κατασκευαστής κλάσης.
     * @param parameters διαχείριση παραμέτρων επισήμανση (Session scope)
     * @param fastAPIManager διαχείριση του FastAPI server (Application scope)
     * @param pipelineService διαχείριση του pipeline για το Stanford CoreNLP (Application Scope)
     */
    public Settings(Parameters parameters, FastAPIManager fastAPIManager, PipeLineForStanford pipelineService) {
        this.parameters = parameters;
        this.pipelineService = pipelineService;
        this.fastAPIManager = fastAPIManager;
        this.setLabelSpacy = new ArrayList<>();
        this.setLabelYolo = new ArrayList<>();
        setPadding(true);
        setSpacing(true);
        setMargin(true);
        setWrap(false);
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        H2 title = new H2(getTranslation("Settings.H2.title"));

        VerticalLayout body = setBody(new VerticalLayout());
        body.setSizeFull();

        HorizontalLayout footer = setFooter(new HorizontalLayout());

        add(title, body , footer);
        setFlexGrow(1,body);

    }

    /**
     * Δημιουργία του κάτω μέρους σελίδας με την αποθήκευση και επαναφορά.
     * Αποθήκευση, ενημερώνεται το Parameters (Session) και στέλνεται αίτημα
     * στον FASTAPI server για φόρτωση των μοντέλων.
     * Επαναφορά, επαναφέρει τις αρχικές ρυθμίσεις (topk=3, threshold = 0.3, spaCy και YOLO)
     * @param footer το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private HorizontalLayout setFooter(HorizontalLayout footer) {
        footer.addClassName("settings-footer");

        save = new Button(getTranslation("Settings.button.save"));
        save.setEnabled(true);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        save.addClickListener(e->{
            parameters.setTopK(Integer.parseInt(topK.getValue()));
            parameters.setThreshold(Float.valueOf(threshold.getValue()/100F));
            parameters.setSpacy(spacy.getValue());
            parameters.setStanfordCoreNLP(spacy.getValue());
            parameters.setYolo(yolo.getValue());
            parameters.setClip(yolo.getValue());
            parameters.setYoloWorld(yolo.getValue());

            String imageModel = parameters.getImageModel().toLowerCase();
            Map<String, String> modelType = fastAPIManager.loadModel(imageModel, "Update_Settings");
            if(modelType.containsKey("errorLoad") || modelType.containsKey("timeout")){
                Notification notification = Notification.show(getTranslation("Settings.errorLoadModelReady"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            String txt = fastAPIManager.convertModel(modelType);
            Notification.show(getTranslation("Settings.Notification.models") + txt, 3000, Notification.Position.BOTTOM_START);

            if(parameters.getStanfordCoreNLP() && !pipelineService.isLoaded()){
                try {
                    pipelineService.init();
                    Notification notification = Notification.show(getTranslation("Settings.Notification.stanford"), 3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }catch(Exception ex){
                   Notification notification = Notification.show(getTranslation("Settings.Notification.errorStanford"), 3000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

                }
            }

            Notification notification = Notification.show(getTranslation("Settings.Notification.saveChanges"), 3000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        });

        defaultButton = new Button(getTranslation("Settings.button.default_"));
        defaultButton.setEnabled(true);
        defaultButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        defaultButton.addClickListener(e->{
            parameters.setTopK(3);
            parameters.setThreshold( 0.30F);
            parameters.setSpacy("spaCy");
            parameters.setStanfordCoreNLP("spaCy");
            parameters.setYolo("YOLO");
            parameters.setClip("YOLO");
            parameters.setYoloWorld("YOLO");
            parameters.setDeepScraping(false);

            String imageModel = parameters.getImageModel().toLowerCase();
            Map<String, String> modelType = fastAPIManager.loadModel(imageModel, "Update_Settings");
            if(modelType.containsKey("errorLoad") || modelType.containsKey("timeout")){
                Notification notification = Notification.show(getTranslation("Settings.errorLoadModelReady"),3000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            String txt = fastAPIManager.convertModel(modelType);
            Notification.show(getTranslation("Settings.Notification.models")  + txt, 3000, Notification.Position.BOTTOM_START);


            Notification notification = Notification.show(getTranslation("Settings.Notification.defaultModels") , 3000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            UI.getCurrent().getPage().reload();
        });

        footer.add(save,defaultButton);

        return footer;
    }

    /**
     * Οργανώνει τις επιμέρους ενότητες σε κάθε διάταξη.
     * @param body το υποδιαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private VerticalLayout setBody(VerticalLayout body){

        VerticalLayout  generalSettings = generalSetting(new VerticalLayout());
        generalSettings.setAlignItems(Alignment.BASELINE);
        applyStyle(generalSettings);
        VerticalLayout model = modelSettings(new VerticalLayout());
        model.setAlignItems(Alignment.BASELINE);
        applyStyle(model);
        VerticalLayout text = textLayout(new VerticalLayout());
        applyStyle(text);
        VerticalLayout image =imageLayout(new VerticalLayout());
        applyStyle(image);

       body.add(generalSettings ,model, text, image);

        body.setSpacing(true);
        body.setPadding(true);

      return body;
    }

    /**
     * Διαμόρφωση της ενότητας γενικών ρυθμίσεων. Γίνεται αλλαγή γλώσσα, η οποία
     * προκαλεί επαναφόρτωση της σελίδας για την εφαρμογή της επιλεγμένης γλώσσας.
     * Ορίζεται ο τρόπος εξαγωγής κειμένου και εικόνας (jsoup / PlayWright) καθώς
     * και η παράκαμψη των πιστοποιητικών SSL.
     * @param layout το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private VerticalLayout generalSetting(VerticalLayout layout){
        H4 title = new H4(getTranslation("Settings.H4.Settings") );
        title.addClassName("main-title");
        HorizontalLayout settings = new HorizontalLayout();

        selectLanguage.setLabel(getTranslation("Settings.SelectLang") );
        selectLanguage.setItems("Ελληνικά" , "English");
        String language;
        if(parameters.getLang().equals("el")){
            language = "Ελληνικά";
        }
        else{
            language = "English";
        }
        selectLanguage.setValue(language);
        selectLanguage.addValueChangeListener(e->{
            String l = e.getValue();
            if(l.equals("English")){
                parameters.setLang("en");
                VaadinSession.getCurrent().setLocale(Locale.forLanguageTag(parameters.getLang()));
            }
            else{
                parameters.setLang("el");
                VaadinSession.getCurrent().setLocale(Locale.forLanguageTag(parameters.getLang()));
            }
            UI.getCurrent().getPage().reload();
        });

        selectScraping.setLabel(getTranslation("Settings.SelectScraping"));
        selectScraping.setValue(parameters.isDeepScraping());
        selectScraping.addValueChangeListener(e->{
            boolean newScraping = e.getValue();
            parameters.setDeepScraping(newScraping);
        });

        ignoreSSL.setLabel(getTranslation("Settings.IgnoreSSL"));
        ignoreSSL.setValue(parameters.isIgnoreSSL());
        ignoreSSL.addValueChangeListener(e->{
            boolean newIgnoreSSL = e.getValue();
            parameters.setIgnoreSSL(newIgnoreSSL);
        });
        ignoreSSL.setTooltipText(getTranslation("Settings.IgnoreSSL.toolTip"));

        settings.add(selectLanguage, selectScraping, ignoreSSL);
        settings.getStyle().setGap("100px");

        settings.getStyle().setAlignItems(Style.AlignItems.BASELINE);
        layout.add(title, settings);
        return layout;
}

    /**
     * Ρύθμιση των παραμέτρων topK και threshold. Για το threshold χρησιμοποιείται
     * το addon paperSlider.
     * @param textLayout
     * @return
     */
    private VerticalLayout modelSettings(VerticalLayout textLayout){

        H4 title = new H4(getTranslation("Settings.H4.topKThreshold") );
        title.addClassName("main-title");
        HorizontalLayout temp = new HorizontalLayout();

        TextField textFieldTopk = new TextField();
        topK.setItems("1","2","3","4","5");
        topK.setLabel(getTranslation("Settings.topK") );
        topK.setValue(String.valueOf(parameters.getTopK()));
        textFieldTopk.setValue(topK.getValue());
        topK.addValueChangeListener(e-> {
            int value = Integer.parseInt(e.getValue());
            textFieldTopk.setValue(""+value);
        });

        threshold = new PaperSlider();
        threshold.setMax(100);
        threshold.setMin(0);
        threshold.setValue((int) (parameters.getThreshold() * 100));
        threshold.setWidth("250px");
        threshold.setLabel(getTranslation("Settings.Threshold") );

        threshold.setPinned(true);
        TextField txtThreshold = new TextField();
        txtThreshold.setLabel(getTranslation("Settings.ThresholdValue"));
        float value_ = threshold.getValue() /100F;
        txtThreshold.setValue(Float.toString(value_));
        threshold.addValueChangeListener(e -> {
            float value = e.getValue() /100F; // εδώ πιάνεις την τιμή
            txtThreshold.setValue(String.format("%.2f",value));
        });

        threshold.getStyle().set("margin-left" , "150px");

        temp.add(topK, threshold, txtThreshold);
        temp.setAlignItems(Alignment.BASELINE);
        textLayout.add(title, temp);
        textLayout.setSpacing(true);
        textLayout.setPadding(true);
        textLayout.setWidthFull();

        return textLayout;

    }

    /**
     * Επιλογή μοντέλου επισήμανση κειμένου.
     * @param textLayout το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private VerticalLayout textLayout(VerticalLayout textLayout){
        H4 title = new H4(getTranslation("Settings.H4.textAnnotationModel"));
        title.addClassName("main-title");

        spacy.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        spacy.setItems("spaCy", "StanfordCoreNLP");
        spacy.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        spacy.setValue(parameters.getTextModel());
        TextField textFieldSpaCy = new TextField();
        textFieldSpaCy.setLabel(getTranslation("Settings.models"));
        textFieldSpaCy.getStyle().set("margin-left" , "150px");
        textFieldSpaCy.setValue(spacy.getValue());
        spacy.addValueChangeListener(e->{
            String value = e.getValue();
            textFieldSpaCy.setValue(value);
            parameters.setSpacy(value);
            parameters.setStanfordCoreNLP(value);});

        HorizontalLayout temp = new HorizontalLayout();
        temp.add( spacy,textFieldSpaCy );
        temp.setAlignItems(Alignment.BASELINE);
        textLayout.add(title,temp);

        textLayout.setSpacing(true);
        textLayout.setPadding(true);
        textLayout.setWidthFull();

        return textLayout;
    }

    /**
     * Επιλογή μοντέλου επισήμανση εικόνων
     * @param imageLayout το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */

    private VerticalLayout imageLayout(VerticalLayout imageLayout){
        H4 title = new H4(getTranslation("Settings.H4.imageAnnotationModel"));
        title.addClassName("main-title");

        yolo.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

       yolo.setItems("YOLO", "CLIP", "YOLO_WORLD");
        //yolo.setItems("YOLO", "YOLO_WORLD");

        yolo.setValue(parameters.getImageModel());
        TextField textFieldYolo = new TextField();

        textFieldYolo.setValue(String.valueOf(yolo.getValue()));
        textFieldYolo.setLabel(getTranslation("Settings.models"));
        textFieldYolo.getStyle().set("margin-left" , "150px");
        yolo.addValueChangeListener(e->{
            String value = e.getValue();

            textFieldYolo.setValue(value);
            parameters.setImageModel(value);

        });

        HorizontalLayout temp = new HorizontalLayout();
        temp.add(yolo,textFieldYolo);
        temp.setAlignItems(Alignment.BASELINE);
        imageLayout.add(title, temp);
        imageLayout.setSpacing(true);
        imageLayout.setPadding(true);
        imageLayout.setWidthFull();

        return imageLayout;

    }

    /**
     * Εφαρμόζει κοινό style (border, background, shadow) στα layout
     * @param layout το προς διαμόρφωση layout
     */
    private void applyStyle(Component layout) {
        layout.getStyle().set("border", "1px solid #e3e3e3");
        layout.getStyle().set("border-radius", "8px");
        layout.getStyle().set("padding", "15px");
   }
}
