package com.example.application.ui.viewStatistics;

import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.statistics.Statistics;
import com.example.application.ui.MainLayout;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.TitleSubtitle;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.io.Serial;
import java.util.*;

/**
 * Σε αυτήν την κλάση εμφανίζονται τα στατιστικά σχετικά με τους τύπους επισημάνσεων
 * για όλο το έργο. Περιλαμβάνει το συνολική ποσοστιαία κατανομή των τύπων
 * επισήμανσης καθώς και την κατανομή ανά τύπο επισήμανσης ανά διεύθυνση.
 */
@Route(value = StatsTypeAnnotation_URLs.ROUTE,  layout = MainLayout.class)
public class StatsTypeAnnotation_URLs extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver{

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "StatsTypeAnnotation_URLs";

    private final HandledProjects handledProjects;
    private Statistics statistics;

    /**
     * Κατασκευαστής κλάσης
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     */
    public StatsTypeAnnotation_URLs(HandledProjects handledProjects){
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
            H2 title = new H2(getTranslation("StatsTypeAnnotation_URLs.H2.title") + " " + projectName);

            add(title);

            if (!projectName.equals("")) {

                add(createView(new VerticalLayout()));
            }
        }
        else{
            Notification notification = Notification.show(getTranslation("CompareLabels.errorData"), 3000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

    }

    /**
     * Δημιουργία της σελίδας απεικόνισης των στατιστικών. Τα διαγράμματα
     * θα φιλοξενηθούν σε δύο layout
     * @param layout το προς διαμόρφωση layout
     * @return το layout με τα δύο διαγράμματα
     */
    private VerticalLayout createView(VerticalLayout layout){
        layout.setSpacing(true);
        layout.setSizeFull();

        Map<String, Integer> typeMap = statistics.getAnnotationType();
        String title2 = getTranslation("StatsTypeAnnotation_URLs.title2");
        VerticalLayout typeLayout = createPieChartAnnotation(new VerticalLayout(), typeMap, title2);

        Map<String, Map<String, Integer>> typeMap1 = statistics.getTypeAnnotationPerUrl();
        String title3 = getTranslation("StatsTypeAnnotation_URLs.title3");
        VerticalLayout typeUrlLayout = createStackedBarAnnotation(new VerticalLayout(), typeMap1,  title3);
        layout.add(typeLayout, typeUrlLayout);

        return layout;
    }

    /**
     * Κατασκευή του διαγράμματος πίτας. Για την απεικόνιση της συχνότητας των
     * τύπων επισήμανσης Map<τύπος επισήμανσης, συχνότητα>
     *  @param layout το προς διαμόρφωση layout
     * @param map χάρτης δεδομένων
     * @param title τίτλος διαγράμματος
     * @return το layout με το διάγραμμα
     */
    private VerticalLayout createPieChartAnnotation(VerticalLayout layout, Map<String, Integer> map, String title){
        H4 tl = new H4(title);
        ApexCharts chart = new ApexCharts();

        String[] labels = map.keySet().toArray(new String[0]);
        Double[] values = map.values().stream().map(Integer::doubleValue).toArray(Double[]::new);

        chart = ApexChartsBuilder.get().withChart(ChartBuilder.get().withStacked(true).withType(Type.PIE).build())
                .withLabels(labels)
                .withDataLabels(DataLabelsBuilder.get().withEnabled(true).build())
                .withLegend(LegendBuilder.get().withPosition(Position.BOTTOM).build()).build();

        chart.setSeries(values);

        chart.setWidth("100%");
        chart.setHeight("400px");

        layout.add(tl, chart);
        return layout;
    }

    /**
     * Δημιουργία Stacked Bar Chart, στο οποίο απεικονίζεται η κατανομή των τύπων
     * επισήμανσης ανά διεύθυνση URL.
     * @param layout το προς διαμόρφωση layout
     * @param map χάρτης δεδομένων
     * @param title τίτλος διαγράμματος
     * @return το layout με το διάγραμμα
     */
    private VerticalLayout createStackedBarAnnotation(VerticalLayout layout, Map<String, Map<String, Integer>> map , String title){

        ApexCharts chart = new ApexCharts();

        List<String> urlsList = new ArrayList<>(map.keySet());

        Set<String> allTypes = new LinkedHashSet<>();
        map.values().forEach(annotation -> allTypes.addAll(annotation.keySet()));

        List<Series<?>> seriesList = new ArrayList<>();

        for(String type : allTypes){
            Double[] values = urlsList.stream().map(url -> map.get(url).getOrDefault(type , 0).doubleValue()).toArray(Double[]::new);

            seriesList.add(new Series<> (type, values));
        }

        chart = ApexChartsBuilder.get().withChart(
                        ChartBuilder.get().withType(Type.BAR).withStacked(true).build())
                .withPlotOptions(PlotOptionsBuilder.get().withBar(BarBuilder.get().withHorizontal(true).build()).build())
                .withXaxis(XAxisBuilder.get().withCategories(urlsList).build())
                .withSeries(seriesList.toArray(new Series[0]))
                .build();


        chart.setWidthFull();
        chart.setHeight("400px");
        TitleSubtitle chartTitle =  new TitleSubtitle();
        chartTitle.setText(title);
        chart.setTitle(chartTitle);

        layout.add(chart);
        return layout;
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
