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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.io.Serial;
import java.util.Map;

/**
 * Στην κλάση LabelsStats περιέχει αναλυτικά στατιστικά για τη συχνότητα εμφάνισης
 * των ετικετών χρήστη. Περιλαμβάνει διαγράμματα για τη συνολική κατάταξη
 * καθώς και για τις επικρατέστερες ετικέτες topK και τις λιγότερες συχνές bottomK
 * ετικέτες.
 */
@Route(value = LabelsStatsUser.ROUTE,  layout = MainLayout.class)
public class LabelsStatsUser extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver{

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROUTE = "UserLabelsStatistics";


    private final HandledProjects handledProjects;
    private Statistics statistics;

    /**
     * Κατασκευαστής κλάσης
     * @param handledProjects διαχείριση έργου, φακέλων και μονοπατιών (Session scope)
     */
    public LabelsStatsUser(HandledProjects handledProjects){
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
            H2 title = new H2(getTranslation("UserLabelStats.H2.title") + " " + projectName);
            title.getStyle().set("margin", "20px");
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
     * Δημιουργία της βασικής όψης της σελίδας. Αποτελείται από τρία οριζόντια
     * layout που φιλοξενούν τα αντίστοιχα κάθετα διαγράμματα Bar. Στα διαγράμματα
     * απεικονίζονται συνολικά οι ετικέτες καθώς και οι επικρατέστερες και οι
     * λιγότερο συχνές ετικέτες.
     * @param layout το προς διαμόρφωση layout
     * @return το διαμορφωμένο layout
     */
    private VerticalLayout createView(VerticalLayout layout){
        layout.setSpacing(true);
        layout.setSizeFull();

        HorizontalLayout horizontalLayout2 = new HorizontalLayout();

        Map<String, Integer> typeMap2_ = statistics.getRankLabelsAnnotations(true);
        String title4_ = getTranslation("UserLabelStats.RankUserLabels");
        VerticalLayout rankLabel_ = createBarChartAnnotation(new VerticalLayout(), typeMap2_, title4_);

        horizontalLayout2.add(rankLabel_);
        horizontalLayout2.setFlexGrow(1, rankLabel_);
        horizontalLayout2.setWidthFull();

        HorizontalLayout horizontalLayout3 = new HorizontalLayout();
        String title5_ = getTranslation("UserLabelStats.TopK");
        Map<String,Integer> integerMap_ = statistics.getTopBottomLabel(3, true, true);
        HorizontalLayout topK_ = createBarTopAnnotations(new HorizontalLayout(), integerMap_, title5_, true, true);

        horizontalLayout3.add(topK_);
        horizontalLayout3.setFlexGrow(1, topK_);
        horizontalLayout3.setWidthFull();

        HorizontalLayout horizontalLayout4 = new HorizontalLayout();
        String title6 = getTranslation("UserLabelStats.BottomK");
        Map<String,Integer> integerMap1 = statistics.getTopBottomLabel(3,  true,false);
        HorizontalLayout bottomK = createBarTopAnnotations(new HorizontalLayout(), integerMap1, title6, true, false);
        horizontalLayout4.add(bottomK);
        horizontalLayout4.setFlexGrow(1, bottomK);

        horizontalLayout4.setWidthFull();

        layout.add(horizontalLayout2, horizontalLayout3, horizontalLayout4);
        return layout;
    }

    /**
     * Δημιουργία κάθετου διαγράμματος. Το Bar Chart χρησιμοποιεί τη βιβλιοθήκη
     * ApexCharts.Τα διαγράμματα απεικονίζουν ένα χάρτη Map<ετικέτα, συχνότητα>.
     * @param layout το προς διαμόρφωση
     * @param map χάρτης δεδομένων
     * @param title τίτλος
     * @return το διαμορφωμένο layout με το διάγραμμα
     */
    private VerticalLayout createBarChartAnnotation(VerticalLayout layout, Map<String, Integer> map , String title){

        ApexCharts chart = new ApexCharts();

        chart = ApexChartsBuilder.get().withChart(ChartBuilder.get().withStacked(true).withType(Type.BAR).build()).
                withPlotOptions(PlotOptionsBuilder.get().withBar(BarBuilder.get().withHorizontal(false).
                        withDistributed(true).build()).build()).build();
        String[] labels = map.keySet().toArray(new String[0]);

        chart.setXaxis(XAxisBuilder.get().withCategories(labels).build());

        Double[] values = map.values().stream().map(Integer :: doubleValue).toArray(Double[]::new);

        chart.setSeries(new Series<>("Annotations" , values));

        chart.setWidth("100%");
        chart.setHeight("400px");
        TitleSubtitle chartTitle =  new TitleSubtitle();
        chartTitle.setText(title);
        chart.setTitle(chartTitle);

        chart.setChart(ChartBuilder.get().withType(Type.BAR).withStacked(true).build());

        layout.add(chart);
        return layout;

    }

    /**
     * Δημιουργείται διάγραμμα με τις topK ή bottomK ετικέτες χρήστη του έργου
     * Το διάγραμμα απεικονίζει χάρτη με κλειδί τις ετικέτες και τιμή τη
     * συχνότητα εμφάνισης. Η επιλογή του αριθμού εμφανίσεων γίνεται με τη
     * βοήθεια ενός comboBox.
     * @param layout το προς διαμόρφωση layout
     * @param map χάρτης δεδομένων
     * @param title τίτλος του διαγράμματος
     * @param isUser true για τις ετικέτες χρήστη, false για τις ετικέτες μοντέλων
     * @param isTop true γις τις topK ετικέτες, false για τις bottomK ετικέτες
     * @return το διαμορφωμένο layout
     */
    private HorizontalLayout createBarTopAnnotations(HorizontalLayout layout, Map<String, Integer> map, String title, boolean isUser, boolean isTop){
        layout.setWidthFull();
        layout.setDefaultVerticalComponentAlignment(Alignment.START);

        VerticalLayout barChartTopKRank = new VerticalLayout();
        barChartTopKRank.setMargin(false);
        barChartTopKRank.setPadding(false);

        barChartTopKRank.add(createBarChartAnnotation(new VerticalLayout(), map, title));
        barChartTopKRank.setPadding(false);
        barChartTopKRank.setMargin(false);

        ComboBox<String> rank = new ComboBox<>();
        rank.setItems("1","2","3","4","5","6","7","8","9","10");
        if(isTop) {
            rank.setLabel(getTranslation("UserLabelStats.combo.topK"));
        }
        else {
            rank.setLabel(getTranslation("UserLabelStats.combo.bottomK"));
        }
        rank.setValue("3");

        rank.addValueChangeListener(e-> {
            int limit = Integer.parseInt(e.getValue());
            Map<String, Integer> newMap = statistics.getTopBottomLabel(limit, isUser, isTop);

            barChartTopKRank.removeAll();
            barChartTopKRank.add(createBarChartAnnotation(new VerticalLayout(), newMap, title));

        });

        layout.add(rank, barChartTopKRank);

        layout.setFlexGrow(1, barChartTopKRank);

        return  layout;

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
