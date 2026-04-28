package com.example.application.ui.viewStatistics;


import com.example.application.backEnd.handledFiles.HandledProjects;
import com.example.application.backEnd.statistics.Statistics;
import com.example.application.ui.MainLayout;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.TitleSubtitle;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.io.Serial;
import java.util.*;

/**
 * Η κλάση General Statistics δίνει τα γενικά στατιστικά του έργου. Αποτυπώνει το
 * σύνολο των επισημάνσεων σε όλο το έργο. Τον αριθμό των διευθύνσεως όπου
 * πραγματοποιήθηκαν οι επισημάνσεις. Την κατανομή των επισημάνσεων ανά σελίδα
 * και την κατανομή των επισημάνσεων που προέκυψαν από συγχώνευση.
 * Για την απεικόνιση των στατιστικών χρησιμοποιείται Stacked Bar Charts από
 * τη βιβλιοθήκη ApexCharts
 */
@Route(value = GeneralStatistics.ROUTE,  layout = MainLayout.class)
public class GeneralStatistics extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver{

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "GeneralStatistics";

    private final HandledProjects handledProjects;
    private Statistics statistics;

    /**
     * Κατασκευαστής κλάσης
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     */
    public GeneralStatistics(HandledProjects handledProjects){
        this.handledProjects = handledProjects;
        this.statistics = new Statistics(handledProjects);
        if(statistics.initAnnotatedDataList()) {

            setPadding(true);
            setSpacing(true);
            setMargin(true);
            setWrap(false);
            setSizeFull();
            setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

            String projectName = handledProjects.getProjectName() != null ? handledProjects.getProjectName() : "";
            H2 title = new H2(getTranslation("GeneralStatistics.H2.title")+" " + projectName);

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
     * Το σώμα της σελίδας, το οποίο αποτελείται από ένα πλαίσιο γενικών πληροφοριών
     * και δύο Stacked Bar Charts για την απεικόνιση των επισημάνσεων στο έργο
     * @param layout το layout που θα δημιουργηθεί το κείμενο
     * @return το διαμορφωμένο layout
     */
    private VerticalLayout createView(VerticalLayout layout){
        layout.setSpacing(true);
        layout.setSizeFull();

        VerticalLayout generalInfo = createGeneralInformation(new VerticalLayout());

        VerticalLayout charts = new VerticalLayout();
        String title = getTranslation("GeneralStatistics.title1");
        Map<String , Integer> pageMap = statistics.getAnnotationsPerPage();
        VerticalLayout annotationPerPage = createBarChartAnnotation(new VerticalLayout(), pageMap, title);
        String title1 = getTranslation("GeneralStatistics.title2");;
        Map<String, Integer> mergedMap = statistics.getMergedAnnotation();
        VerticalLayout mergedAnnotation = createBarChartAnnotation(new VerticalLayout(), mergedMap, title1);

        charts.add(annotationPerPage, mergedAnnotation);
        charts.setFlexGrow(1, annotationPerPage);
        charts.setFlexGrow(1, mergedAnnotation);

        layout.add(generalInfo, charts);
        return layout;
    }

    /**
     * Δημιουργία κειμένου για την εμφάνιση του συνολικού αριθμού επισημάνσεων
     * που έχουν γίνει και τον αριθμό των ιστοσελίδων
     * @param layout το layout που θα δημιουργηθεί το κείμενο
     * @return το διαμορφωμένο layout
     */
    private VerticalLayout createGeneralInformation(VerticalLayout layout){

        String projectName = handledProjects.getProjectName() != null ? handledProjects.getProjectName() : "";
        String msg = projectName.equals("") ? "" :
                getTranslation("GeneralStatistics.textInfo1")+" " + handledProjects.getProjectName() +" "+ getTranslation("GeneralStatistics.textInfo2") +" "
                        + statistics.getTotAnnotations()  + " " + getTranslation("GeneralStatistics.textInfo3")+" "
                        +statistics.getTotPages() + " " + getTranslation("GeneralStatistics.textInfo4");
        Span message = new Span();
        message.setText(msg);

        layout.setPadding(true);
        layout.setSpacing(true);

        layout.add(message);

        return layout;
    }

    /**
     * Δημιουργία του Stacked Bar Charts. Το γράφημα δείχνει ένα χάρτη όπου
     * το κλειδί είναι οι διευθύνσεις που έγιναν επισημάνσεις και οι τιμές είναι
     * το σύνολο των επισημάνσεων που έγιναν στη συγκεκριμένη ιστοσελίδα.
     * @param layout το προς διαμόρφωση layout
     * @param map ο χάρτης δεδομένων Map<url, rank>
     * @param title τίτλος γραφήματος
     * @return το διαμορφωμένο layout
     */
    private VerticalLayout createBarChartAnnotation(VerticalLayout layout, Map<String, Integer> map , String title){

        ApexCharts chart = new ApexCharts();

        chart = ApexChartsBuilder.get().withChart(ChartBuilder.get().withStacked(true).withType(Type.BAR).build()).
                withPlotOptions(PlotOptionsBuilder.get().withBar(BarBuilder.get().withHorizontal(true).
                        withDistributed(true).build()).build()).build();
        String[] labels = map.keySet().toArray(new String[0]);

        chart.setXaxis(XAxisBuilder.get().withCategories(labels).build());

        Double[] values = map.values().stream().map(Integer :: doubleValue).toArray(Double[]::new);

        chart.setSeries(new Series<>("Annotations" , values));

        chart.setWidth("100%");
        chart.setHeight("400px");

        chart.setChart(ChartBuilder.get().withType(Type.BAR).withStacked(true).build());
        TitleSubtitle chartTitle =  new TitleSubtitle();
        chartTitle.setText(title);
        chart.setTitle(chartTitle);

        //layout.add(tl, chart);
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
