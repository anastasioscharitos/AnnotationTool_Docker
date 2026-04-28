package com.example.application.ui.viewStatistics;

import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.statistics.Statistics;
import com.example.application.ui.MainLayout;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.TitleSubtitle;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.PlotOptionsBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.io.Serial;
import java.util.*;

/**
 * Αυτή η κλάση συγκρίνει τις ετικέτες του έργου.
 * Χρησιμοποιείται Stacked Bar Charts από τη βιβλιοθήκη ApexCharts για την
 * απεικόνιση της συχνότητας εμφάνισης των ετικετών ανά τύπο επισήμανσης.
 * Κατασκευάζονται διαφορετικά διαγράμματα για τις ετικέτες του χρήστη και των μοντέλων
 * Υπάρχει η δυνατότητα να εμφανιστεί η συχνότητα και για συγκεκριμένη διεύθυνση url.
 * Για τη σωστή εμφάνιση των γραφημάτων πραγματοποιείται αυτόματη εναλλαγή
 * σε Light Mode.
 */
@Route(value = CompareLabels.ROUTE,  layout = MainLayout.class)
public class CompareLabels extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver{

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "CompareLabels";

    private final HandledProjects handledProjects;
    private Statistics statistics;
    private MultiSelectComboBox<String> labels = new MultiSelectComboBox<>();
    private MultiSelectComboBox<String> userLabels = new MultiSelectComboBox<>();
    private ComboBox<String> urls = new ComboBox<>();
    private List<String> listLabels = new ArrayList<>();
    private List<String> listUserLabels = new ArrayList<>();
    private String url;
    private boolean flagAddAllModel = false;
    private boolean flagAddAllUser = false;

    private VerticalLayout layoutLabel = new VerticalLayout();
    private VerticalLayout layoutUserLabel = new VerticalLayout();
    private VerticalLayout layoutUrls = new VerticalLayout();
    /**
     * Κατασκευαστής κλάσης
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     */
    public CompareLabels(HandledProjects handledProjects) {
        this.handledProjects = handledProjects;
        this.statistics = new Statistics(handledProjects);

        if(statistics.initAnnotatedDataList()) {
            setPadding(true);
            setSpacing(true);
            setMargin(true);
            setWrap(false);
            setSizeFull();
            setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);

            String projectName = handledProjects.getProjectName() != null ? handledProjects.getProjectName() : "";
            H2 title = new H2(getTranslation("CompareLabel.H2.title") + " " + projectName);

            add(title);

            if (!projectName.equals("")) {
                HorizontalLayout combo = comboBoxes(new HorizontalLayout());
                combo.setWidthFull();
                combo.setFlexGrow(0);

                VerticalLayout chartLayout = new VerticalLayout();

                chartLayout.add(layoutLabel, layoutUserLabel, layoutUrls);
                chartLayout.setPadding(false);
                chartLayout.setSpacing(true);
                chartLayout.getStyle().set("overflow", "auto");

                add(combo, chartLayout);
                setFlexGrow(1, chartLayout);

            }
        }
        else{
            Notification notification = Notification.show(getTranslation("CompareLabels.errorData"), 3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Δημιουργία του Stacked Bar Charts. Το γράφημα δείχνει ένα Map όπου
     * κλειδί είναι οι ετικέτες και οι τιμές είναι ένα εσωτερικό LinkedMap με τη
     * συχνότητα εμφάνισης ανά τύπο επισήμανσης.
     * Map<label, Map<type_Annotation, rank>>
     * @param map χάρτης με τα δεδομένα
     * @param title τίτλος του γραφήματος
     * @return το ApexChart
     */
    private ApexCharts createLabelsCharts(Map<String, Map<String, Integer>> map, String title){

        ApexCharts chart = new ApexCharts();

        List<String> labelList = new ArrayList<>(map.keySet());

        Set<String> allType = new LinkedHashSet<>();
        map.values().forEach(annotation -> allType.addAll(annotation.keySet()));

        List<Series<?>> seriesList = new ArrayList<>();

        for(String type: allType){
            Double[] values = labelList.stream().map(label -> map.get(label).getOrDefault(type , 0).doubleValue()).toArray(Double[]::new);
            seriesList.add(new Series<>(type, values));
        }

    chart = ApexChartsBuilder.get().withChart(
            ChartBuilder.get().withType(Type.BAR).withStacked(true).build())
                    .withPlotOptions(PlotOptionsBuilder.get().withBar(BarBuilder.get().withHorizontal(true).build()).build())
            .withXaxis(XAxisBuilder.get().withCategories(labelList).build())
            .withSeries(seriesList.toArray(new Series[0]))
            .build();

        chart.setWidthFull();
        int height = Math.max(400 , labelList.size() * 20);
        chart.setHeight(height + "px");
        TitleSubtitle chartTitle =  new TitleSubtitle();
        chartTitle.setText(title);
        chart.setTitle(chartTitle);

        return chart;
    }

    /**
     * Δημιουργείται το layout με τα comboBox (επιλογή διεύθυνσης) και
     * MultiComboBox (επιλογή ετικετών των μοντέλων και του χρήστη)
     * @param horizontalLayout το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private HorizontalLayout comboBoxes(HorizontalLayout horizontalLayout) {
    horizontalLayout.setWidthFull();
    horizontalLayout.setSpacing(true);

    createMultiComboBox(labels, getTranslation("CompareLabel.comboLabel.classModel"), statistics.getLabels(false), listLabels, false);
    createMultiComboBox(userLabels, getTranslation("CompareLabel.comboLabel.classUser"), statistics.getLabels(true), listUserLabels, true);
    createComboBox(urls, getTranslation("CompareLabel.comboLabel.url"), statistics.getAllUrls());

    horizontalLayout.add(labels, userLabels, urls);

    return horizontalLayout;
}

    /**
     * Δημιουργείται το comboBox με τις διευθύνσεις. Κάθε φορά που επιλέγεται
     * κάποια διεύθυνση γίνεται refresh στο layout.
     * @param comboBox comboBox που θα δημιουργηθεί
     * @param strLabel η ετικέτα του comboBox
     * @param setLabel το Set των διευθύνσεων
     * @return comboBox
     */
    private ComboBox<String> createComboBox(ComboBox<String> comboBox, String strLabel, Set<String> setLabel){
        comboBox.setLabel(strLabel);
        comboBox.setItems(setLabel);

        comboBox.addValueChangeListener(select ->{
            url = select.getValue();

            refreshLayouts();
        });
        return comboBox;
}

    /**
     * Δημιουργία MultiComboBox για τις ετικέτες μοντέλων και του χρήστη.
     * Στις επιλογές της λίστας του MultiComboBox έχει προστεθεί και η επιλογή
     * "Επιλογή Όλων" για την επιλογή όλων των στοιχείων της λίστας.
     * Έχουν προστεθεί δύο listener. Ο ValueChangeListener πιάνει τις αλλαγές
     * επιλογές του MultiComboBox. Όταν επιλέγεται "Επιλογή όλων" τότε επιλέγονται όλα
     * τα στοιχεία της λίστας ενώ όταν απoεπιλέγεται τότε απoεπιλέγονται όλα τα στοιχεία.
     * O SelectionListener πιάνει τις επιλογές (αν υπάρχει η "Επιλογή όλων"
     * τότε αφαιρείται) του MultiComboBox
     * @param comboBox το multiComboBox που θα κατασκευαστεί
     * @param strLabel η ετικέτα του
     * @param setLabels το Set τιμών
     * @param selections λίστα επιλογής
     * @param isUser boolean αν πρόκειται για τν χρήστη
     * @return το multiComboBox
     */
    private MultiSelectComboBox<String> createMultiComboBox(MultiSelectComboBox<String> comboBox, String strLabel, Set<String> setLabels, List<String> selections, boolean isUser) {
        comboBox.setLabel(strLabel);
        String selectAddAll = getTranslation("CompareLabels.selectAddAll");
        setLabels.add(selectAddAll);
        comboBox.setItems(setLabels);

        comboBox.addValueChangeListener( value->{
            Set<String> selected = value.getValue();

            if(selected.contains(selectAddAll)){

                comboBox.select(setLabels);
                if(isUser) {
                    flagAddAllUser = true;
                }
                else{
                    flagAddAllModel = true;
                }
            } else if (selected.isEmpty()) {
                comboBox.clear();
            }
            else if(isUser && flagAddAllUser && !selected.contains(selectAddAll)){
                flagAddAllUser = false;
                comboBox.deselectAll();
                comboBox.clear();
            }
            else if(!isUser && flagAddAllModel && !selected.contains(selectAddAll)){
                flagAddAllModel = false;
                comboBox.deselectAll();
                comboBox.clear();
            }
        });

        comboBox.addSelectionListener(select -> {
            selections.clear();
            List<String> list = new ArrayList<>(select.getAllSelectedItems());
            Collections.sort(list);
            selections.addAll(list);
            if(selections.contains(selectAddAll)){
                selections.remove(selectAddAll);
            }

            refreshLayouts();
        });

        return comboBox;
    }

    /**
     * Ανανέωση του layout, κάθε φορά που αλλάζει ο listener ανανεώνεται το
     * Stacked Bar Charts
     */
    private void refreshLayouts(){
        layoutUrls.removeAll();
        layoutLabel.removeAll();
        layoutUserLabel.removeAll();

        if(!listLabels.isEmpty()){
            Map<String, Map<String, Integer>> map = new HashMap<>();
            map = statistics.getTypeAnnotationPerLabel(listLabels, false);
            layoutLabel.setHeightFull();
            layoutLabel.add(createLabelsCharts(map, getTranslation("CompareLabel.mapLabel.ModelClass")));
        }

        if(!listUserLabels.isEmpty()){
            Map<String, Map<String, Integer>> map = new HashMap<>();
            map = statistics.getTypeAnnotationPerLabel(listUserLabels, true);
            layoutUserLabel.setHeightFull();
            layoutUserLabel.add(createLabelsCharts(map, getTranslation("CompareLabel.mapLabel.ModelUser")));
        }

        if(url != null  && !listLabels.isEmpty()) {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            map = statistics.getTypeAnnotationPerLabelForUrl(listLabels, url, false);
            layoutUrls.setHeightFull();
            layoutUrls.add(createLabelsCharts(map, getTranslation("CompareLabel.mapLabelURL1") +" " + url + " " + getTranslation("CompareLabel.mapLabelURL2")));
        }

        if(url != null && !listUserLabels.isEmpty()) {
            Map<String, Map<String, Integer>> map_ = new HashMap<>();
            map_ = statistics.getTypeAnnotationPerLabelForUrl(listUserLabels, url, true);
           layoutUrls.setHeightFull();
            layoutUrls.add(createLabelsCharts(map_, getTranslation("CompareLabel.mapLabelURL3") +" " +url + " " + getTranslation("CompareLabel.mapLabelURL2")));

        }
    }

    /**
     *Επιβάλλει το Light Mode κατά την είσοδο στη σελίδα. Αυτό είναι απαραίτητο
     * για τη σωστή απόδοση των χρωμάτων και των αξόνων των ApexChart
     * @param event
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event){
        UI ui = event.getUI();
        ui.getElement().executeJs("document.documentElement.setAttribute('force-light', '');" +
                "if(window.applySystemTheme) window.applySystemTheme(window.matchMedia('(prefers-color-scheme: dark)'));");
        ui.getElement().setAttribute("theme", "light");
        ui.getElement().getThemeList().remove("dark");
    }

    /**
     * Επαναφέρει τις ρυθμίσεις του χρήστη (Dark/Light Mode) κατά την έξοδο
     * @param event
     */
    @Override
    public void beforeLeave(BeforeLeaveEvent event){
        UI ui = event.getUI();

        ui.getElement().executeJs("document.documentElement.removeAttribute('force-light');");

        ui.getElement().executeJs("window.applySystemTheme(window.matchMedia('(prefers-color-scheme: dark)'));");
    }

}