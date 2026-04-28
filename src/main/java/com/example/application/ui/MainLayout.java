package com.example.application.ui;

import com.example.application.backEnd.handledFiles.HandledLockProject;
import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.settings.Parameters;
import com.example.application.ui.viewStatistics.*;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.VaadinSession;

import java.util.Locale;

/**
 * Η κλάση είναι υπεύθυνη για τη δημιουργία του πλαϊνού μενού πλοήγησης και της γραμμής
 * εργαλείων. Διαχειρίζεται την εναλλαγή Dark/Light βάση των ρυθμίσεων του browser.
 * Υπάρχει μηχανισμός ασφαλούς ξεκλειδώματος του έργου όταν λήξει το Session,
 * χρησιμοποιώντας SessionDestroyListener
 */
public class MainLayout extends AppLayout {

    private H2 title;
    private final Parameters parameters;
    private final HandledLockProject lockProject;
    private final HandledProjects handledProjects;

    /**
     * Κατασκευαστής κλάσης
     * @param parameters διαχείριση παραμέτρων επισήμανσης (Session scope)
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     * @param lockProject διαχείριση κλειδώματος (Application scope)
     */
    public MainLayout(Parameters parameters, HandledProjects handledProjects, HandledLockProject lockProject){
        this.parameters = parameters;
        this.handledProjects = handledProjects;
        this.lockProject = lockProject;

        VaadinSession.getCurrent().setLocale(Locale.forLanguageTag(parameters.getLang()));

        addToNavbar(new DrawerToggle());
        title = new H2(getTranslation("MainLayout.Title"));
        title.addClassName("navbar-title");
        addToNavbar(title);

        SideNav newProject = new SideNav();
        newProject.setWidthFull();

        newProject.addItem(new SideNavItem(getTranslation("MainLayout.HomePage"), HomePage.class, VaadinIcon.HOME.create()));

        SideNavItem annotationRoot = new SideNavItem(getTranslation("MainLayout.Annotation"));
        annotationRoot.setPrefixComponent(VaadinIcon.PENCIL.create());
        annotationRoot.addItem(createItem(getTranslation("ManiLayout.Annotation.textAnnotation"), UrlsTextAnnotation.class, VaadinIcon.FILE_TEXT, getTranslation("ManiLayout.Annotation.textAnnotation.toolTip")));
        annotationRoot.addItem(createItem(getTranslation("ManiLayout.Annotation.imageAnnotation"), UrlsImageAnnotation.class, VaadinIcon.PICTURE, getTranslation("ManiLayout.Annotation.imageAnnotation.toolTip")));
        annotationRoot.addItem(createItem(getTranslation("ManiLayout.Annotation.imageWithTextAnnotation"), ImageWithTextAnnotation.class, VaadinIcon.TEXT_INPUT, getTranslation("ManiLayout.Annotation.imageWithTextAnnotation.toolTip")));
        annotationRoot.addItem(createItem(getTranslation("ManiLayout.Annotation.mergeAnnotation"), LoadData.class, VaadinIcon.COMPRESS, getTranslation("ManiLayout.Annotation.mergeAnnotation.toolTip")));
        newProject.addItem(annotationRoot);

        SideNavItem uploadRoot = new SideNavItem(getTranslation("MainLayout.Load"));
        uploadRoot.setPrefixComponent(VaadinIcon.UPLOAD.create());
        uploadRoot.addItem(createItem(getTranslation("MainLayout.Load.url"), UpLoadUrls.class, VaadinIcon.LINK,getTranslation("MainLayout.Load.url.toolTip")));
        uploadRoot.addItem(createItem(getTranslation("MainLayout.Load.dictionary"), UploadWord.class, VaadinIcon.LINK, getTranslation("MainLayout.Load.dictionary.toolTip")));
        newProject.addItem(uploadRoot);


        SideNavItem statsRoot = new SideNavItem(getTranslation("MainLayout.Statistics"));
        statsRoot.setPrefixComponent(VaadinIcon.PIE_BAR_CHART.create());
        statsRoot.addItem(createItem(getTranslation("MainLayout.Statistics.general"), GeneralStatistics.class, VaadinIcon.BAR_CHART_H, getTranslation("MainLayout.Statistics.general.toolTip")));
        statsRoot.addItem(createItem(getTranslation("MainLayout.Statistics.modelsAnnotation"), StatsTypeAnnotation_URLs.class, VaadinIcon.PIE_BAR_CHART, getTranslation("MainLayout.Statistics.modelsAnnotation.toolTip")));
        statsRoot.addItem(createItem(getTranslation("MainLayout.Statistics.modelLabels"), LabelsStats.class, VaadinIcon.BAR_CHART, getTranslation("MainLayout.Statistics.modelLabels.toolTip")));
        statsRoot.addItem(createItem(getTranslation("MainLayout.Statistics.userLabels"), LabelsStatsUser.class, VaadinIcon.BAR_CHART, getTranslation("MainLayout.Statistics.userLabels.toolTip")));
        statsRoot.addItem(createItem(getTranslation("MainLayout.Statistics.compareLabels"), CompareLabels.class, VaadinIcon.BAR_CHART_V, getTranslation("MainLayout.Statistics.compareModels.toolTip")));
        statsRoot.addItem(createItem(getTranslation("MainLayout.Statistics.compareUrl"), CompareUrls.class, VaadinIcon.BAR_CHART_V ,getTranslation("MainLayout.Statistics.compareUrl.toolTip")));
        statsRoot.addItem(createItem(getTranslation("MainLayout.Statistics.HeatMap"), HeatMapLabels.class, VaadinIcon.RASTER, getTranslation("MainLayout.Statistics.HeatMap.toolTip")));

        newProject.addItem(statsRoot);

        SideNavItem paramsRoot = new SideNavItem(getTranslation("MainLayout.Parameters"));
        paramsRoot.setPrefixComponent(VaadinIcon.COG.create());
        paramsRoot.addItem(createItem(getTranslation("MainLayout.Parameters.settings"), Settings.class, VaadinIcon.SLIDERS, getTranslation("MainLayout.Parameters.settings.toolTip")));
        paramsRoot.addItem(createItem(getTranslation("MainLayout.Parameters.modelsLabels"), ModelsLabels.class, VaadinIcon.AUTOMATION, getTranslation("MainLayout.Parameters.modelsLabels.toolTip")));
        newProject.addItem(paramsRoot);

        VerticalLayout drawer = new VerticalLayout(newProject);
        drawer.setPadding(true);
        drawer.setSpacing(false);

        addToDrawer(drawer);

        UI.getCurrent().getElement().executeJs("""
            window.applySystemTheme = (e) => {
                // Αν η σελίδα έχει το attribute 'force-light', σταματάμε εδώ
                if (document.documentElement.hasAttribute('force-light')) {
                    document.documentElement.setAttribute('theme', 'light');
                    return;
                }
                if (e.matches) {
                    document.documentElement.setAttribute('theme', 'dark');
                } else {
                    document.documentElement.removeAttribute('theme');
                }
            };
            const mq = window.matchMedia('(prefers-color-scheme: dark)');
            window.applySystemTheme(mq);
            mq.addListener(window.applySystemTheme);
        """);
    }

    /**
     * Η μέθοδος δημιουργεί στοιχεία στο πλαϊνό menu.
     * @param title ο τίτλος
     * @param class_ η κλάση στην οποία οδηγεί το στοιχείο
     * @param icon το εικονίδιο
     * @param toolTipTxt βοηθητικό κείμενο
     * @return το διαμορφωμένο SideNavItem
     */
    private SideNavItem createItem(String title, Class<?> class_, VaadinIcon icon, String toolTipTxt) {
        SideNavItem temp = new SideNavItem(title, (Class<? extends Component>) class_, icon.create());
        temp.setTooltipText(toolTipTxt);
        return  temp;
    }

    }