package com.example.application.ui.viewStatistics;


import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.statistics.Statistics;
import com.example.application.ui.MainLayout;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.xaxis.builder.LabelsBuilder;
import com.github.appreciated.apexcharts.config.xaxis.builder.TitleBuilder;
import com.github.appreciated.apexcharts.config.yaxis.*;
import com.github.appreciated.apexcharts.helper.Coordinate;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.UI;
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
import java.util.stream.Collectors;

/**
 * Η κλάση HeatMapLabels δημιουργεί τρία heatMap όπου συγκρίνονται οι ετικέτες
 * μεταξύ του. Ετικέτες Μοντέλων vs Ετικέτες Μοντέλων, Ετικέτες Χρήστη vs Ετικέτες Χρήστη
 * και Ετικέτες Μοντέλων vs Ετικέτες Χρήστη. Για την απεικόνιση χρησιμοποιείται
 * το HeatMap από τη βιβλιοθήκη ApexChart.
 */
@Route(value = HeatMapLabels.ROUTE,  layout = MainLayout.class)
public class HeatMapLabels extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver{

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "CompareLabelsHeatMap";

    private final HandledProjects handledProjects;
    private Statistics statistics;
    private MultiSelectComboBox<String> labels = new MultiSelectComboBox<>();
    private MultiSelectComboBox<String> userLabels = new MultiSelectComboBox<>();
    private List<String> listLabels = new ArrayList<>();
    private List<String> listUserLabels = new ArrayList<>();
    private VerticalLayout layoutLabel = new VerticalLayout();
    private VerticalLayout layoutUserLabel = new VerticalLayout();
    private VerticalLayout layoutModelVsUser = new VerticalLayout();

    private boolean flagAddAllModel = false;
    private boolean flagAddAllUser = false;

    /**
     * Κατασκευαστής κλάσης
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     */
    public HeatMapLabels(HandledProjects handledProjects) {
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
            H2 title = new H2(getTranslation("HeatMap.H2.title") + " " + projectName);
            title.getStyle().set("margin", "20px");

            add(title);

            if (!projectName.equals("")) {
                HorizontalLayout combo = comboBoxes(new HorizontalLayout());
                combo.setWidthFull();
                combo.setFlexGrow(0);

                VerticalLayout chartLayout = new VerticalLayout();

                chartLayout.add(layoutLabel, layoutUserLabel, layoutModelVsUser);
                chartLayout.setPadding(false);
                chartLayout.setSpacing(true);
                chartLayout.getStyle().set("overflow", "auto");

                add(combo, chartLayout);
                setFlexGrow(1, chartLayout);

            }
        }else{
            Notification notification = Notification.show(getTranslation("CompareLabels.errorData"), 3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

    }

    /**
     * Δημιουργία του layout που περιέχει τα MultiComboBox για τις ετικέτες
     * των μοντέλων και του χρήστη.
     * @param horizontalLayout το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private HorizontalLayout comboBoxes(HorizontalLayout horizontalLayout) {
        horizontalLayout.setWidthFull();
        horizontalLayout.setSpacing(true);

        createMultiComboBox(labels, getTranslation("HeatMap.comboLabel.classModel"), statistics.getLabels(false), listLabels, false);
        createMultiComboBox(userLabels, getTranslation("HeatMap.comboLabel.classUser"), statistics.getLabels(true), listUserLabels, true);

        horizontalLayout.add(labels, userLabels);

        return horizontalLayout;
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
     * @param comboBox ο multiComboBox που θα κατασκευαστεί
     * @param strLabel η ετικέτα του
     * @param setLabels το Set τιμών
     * @param selections λίστα επιλογής
     * @return ο multiComboBox
     */
    private MultiSelectComboBox<String> createMultiComboBox(MultiSelectComboBox<String> comboBox,
                                                            String strLabel, Set<String> setLabels, List<String> selections, boolean isUser) {
        comboBox.setLabel(strLabel);
        comboBox.setItems(setLabels);
        String selectAddAll = getTranslation("CompareLabels.selectAddAll");
        setLabels.add(selectAddAll);

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
     * HeatMap
     */
    private void refreshLayouts(){
        String labelModel = getTranslation("HeatMap.LabelsModel");
        String labelUser = getTranslation("HeatMap.LabelsUser");
        layoutLabel.removeAll();
        layoutUserLabel.removeAll();
        layoutModelVsUser.removeAll();

        if(!listLabels.isEmpty()){

            Map<String, Map<String, Integer>> map = new HashMap<>();
            map = statistics.correlationMatrixLabels(listLabels, false);
            layoutLabel.setHeightFull();
            layoutLabel.add(createHeatMap(map, getTranslation("HeatMap.mapLabel.ClassModel"), labelModel, labelModel));
        }

        if(!listUserLabels.isEmpty()){

            Map<String, Map<String, Integer>> map = new HashMap<>();
            map = statistics.correlationMatrixLabels(listUserLabels, true);
            layoutUserLabel.setHeightFull();
            layoutUserLabel.add(createHeatMap(map, getTranslation("HeatMap.mapLabel.ClassUser"),labelUser, labelUser));
        }

        if(!listLabels.isEmpty() && !listUserLabels.isEmpty()){

            Map<String, Map<String, Integer>> map = new HashMap<>();
            map = statistics.correlationModelLabelVsUser(listLabels, listUserLabels);
            layoutModelVsUser.setHeightFull();
            layoutModelVsUser.add(createHeatMap(map, getTranslation("HeatMap.mapLabel.ClassModelVsUser"), labelUser, labelModel));

        }
    }

    /**
     * Δημιουργείται ένα HeatMap για τη σύγκριση ετικετών. Το γράφημα απεικονίζει
     * τη συσχέτιση μεταξύ δύο συνόλων δεδομένων (ετικετών χρήστη και μοντέλων.).
     * Ο αριθμός σε κάθε τετράγωνο υποδεικνύει τη συχνότητα εμφάνισης της ετικέτας.
     * @param matrix χάρτης δεδομένων Map<ετικέτα, Μαπ<ετικέτα, συχνότητα>>
     * @param title τίτλος
     * @return το heatMap
     */
    private ApexCharts createHeatMap(Map<String, Map<String, Integer>> matrix, String title, String labelsX, String labelsY){
        ApexCharts chart = new ApexCharts();
        List<Series<?>> seriesList = new ArrayList<>();

        com.github.appreciated.apexcharts.config.yaxis.Title yTitle = new com.github.appreciated.apexcharts.config.yaxis.Title();
        yTitle.setText(labelsY);

        for(Map.Entry<String, Map<String, Integer>> entry : matrix.entrySet()){
            String row = entry.getKey();
            Map<String, Integer> column = entry.getValue();

            Coordinate[] coordinates = entry.getValue().entrySet().stream()
                    .map(e -> new Coordinate(e.getKey(), e.getValue()))
                    .toArray(Coordinate[]::new);

            seriesList.add(new Series<>(row, (Object[]) coordinates));
        }
        chart = ApexChartsBuilder.get().withChart(ChartBuilder.get().withType(Type.HEATMAP).build())
                .withDataLabels(DataLabelsBuilder.get().withEnabled(true).build())
                .withColors("#BA5C8E")
                .withSeries(seriesList.toArray(new Series[0]))
                .withTitle(TitleSubtitleBuilder.get().withText(title).build())
                .withXaxis(XAxisBuilder.get().
                        withTitle(TitleBuilder.get().withText(labelsX).build())
                        .withLabels(LabelsBuilder.get().withRotate(-45.0).build())
                        .build())
                .withYaxis(YAxisBuilder.get().withTitle(yTitle).build())
                .build();

        chart.setWidthFull();
        int height = Math.max(500 , seriesList.size() * 20);
        chart.setHeight(height + "px");
        return chart;
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
