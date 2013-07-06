package org.altervista.bertuz83.sgaget.charts;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.altervista.bertuz83.sgaget.R;
import org.altervista.bertuz83.sgaget.business.TransportationStatistics;
import org.altervista.bertuz83.sgaget.helper.UtilityFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * User: bertuz
 * Project: sgaget
 *
 * classe per la generazione dei grafici degli spostamenti personali tramite la libreria aChartEngine.
 * Workaround dovuto a bug che non consentirebbe l'ottenimento del bitmap del grafico (utilizzato nella condivisione del grafico sui social network)
 */
public class ChartEngine {
    private Context context;
    private List<long[]> values;
    private double max= 0.0;
    private String da= "";
    private String a="";

    private boolean shareChart;

    public ChartEngine(Context activityContext) {
        this.context= activityContext;
    }


    public GraphicalView getChart(TransportationStatistics statistics, boolean shareChart){
        this.shareChart= shareChart;

        this.values = new ArrayList<long[]>();
        values.add(new long[] {
                UtilityFunctions.getMinutesFromLong(new Double(statistics.getAuto()).longValue()),
                UtilityFunctions.getMinutesFromLong(new Double(statistics.getAutobus()).longValue()),
                UtilityFunctions.getMinutesFromLong(new Double(statistics.getBici()).longValue()),
                UtilityFunctions.getMinutesFromLong(new Double(statistics.getPiedi()).longValue()),
                UtilityFunctions.getMinutesFromLong(new Double(statistics.getAltro()).longValue())});

        max= UtilityFunctions.getMinutesFromLong(new Double(statistics.getMax()).longValue());

        da= statistics.getDa();
        a= statistics.getA();

        GraphicalView gv= createIntent();

        return gv;
    }


    /*
     scritta per creare una possibile comparazione con uno o più tragitti in futuro
    */
    public GraphicalView createIntent() {
        String[] seriesTitles = new String[] {"Tragitto " + da + "-" + a};
        int[] barColors = new int[] { Color.parseColor("#a4c739")};

        XYMultipleSeriesRenderer renderer= buildBarRenderer(values.size());
        setChartSettings(renderer, "", "", "Media (minuti)", 0.5, 5.5, 0, max, Color.BLACK, Color.BLACK);
        renderer.setXLabels(0);
        renderer.setYLabels(0);
        renderer.addXTextLabel(1, context.getString(R.string.transportation_type_car));
        renderer.addXTextLabel(2, context.getString(R.string.transportation_type_bus));
        renderer.addXTextLabel(3, context.getString(R.string.transportation_type_bycicle));
        renderer.addXTextLabel(4, context.getString(R.string.transportation_type_feet));
        renderer.addXTextLabel(5, "altro");

        //per ogni serie visualizzata, imposto alcune personalizzazioni (per ora tutte simili, visualizzandone solo una)
        int length = renderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
            SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
            seriesRenderer.setDisplayChartValues(true);
            seriesRenderer.setChartValuesTextSize(20);
            seriesRenderer.setColor(barColors[i]);
        }

        final GraphicalView grfv = ChartFactory.getBarChartView(context, buildBarDataset(seriesTitles, values), renderer, BarChart.Type.DEFAULT);

        if(shareChart){
            grfv.setDrawingCacheEnabled(true);
            grfv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            grfv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            grfv.layout(0, 0, 800, 600);
            grfv.buildDrawingCache(true);
        }

        return grfv;
    }


    protected XYMultipleSeriesRenderer buildBarRenderer(int nrSeries) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        for (int i = 0; i < nrSeries; i++) {
            SimpleSeriesRenderer r = new SimpleSeriesRenderer();
            //r.setChartvalueAngle(-90);
            r.setChartValuesSpacing(15);
            renderer.addSeriesRenderer(r);
        }
        return renderer;
    }


    protected XYMultipleSeriesDataset buildBarDataset(String[] seriesTitles, List<long[]> values) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        int length = seriesTitles.length;

        for (int i = 0; i < length; i++) {
            CategorySeries series = new CategorySeries(seriesTitles[i]);
            long[] v = values.get(i);
            int seriesLength = v.length;
            for (int j = 0; j < seriesLength; j++) {
                series.add(v[j]);
            }
            dataset.addSeries(series.toXYSeries());
        }
        return dataset;
    }


    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
                                    String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
                                    int labelsColor) {
        renderer.setChartTitle(title);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setMargins(new int[]{30, 0, 0, 0});
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
        renderer.setPanEnabled(false, false);
        renderer.setZoomEnabled(false, false);
        renderer.setApplyBackgroundColor(false);
        renderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
        renderer.setShowLegend(shareChart);
        renderer.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
        //settings statici non parametrizzati
        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        renderer.setBarSpacing(1);
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0,Color.BLACK);

        if(shareChart){
            renderer.setBackgroundColor(Color.WHITE);
            renderer.setApplyBackgroundColor(true);
        }
    }
}
