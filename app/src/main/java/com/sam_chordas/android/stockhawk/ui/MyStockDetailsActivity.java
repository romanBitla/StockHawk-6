package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.Utility;
import com.sam_chordas.android.stockhawk.jsonpojo.HistoricalDataPojo;
import com.sam_chordas.android.stockhawk.jsonpojo.Quote;
import com.sam_chordas.android.stockhawk.retrointerface.HistoricalDataInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
/**
 * Created by Vidhya on 13-11-2016.
 */

public class MyStockDetailsActivity extends AppCompatActivity implements Callback<HistoricalDataPojo> {
    public static final String STOCK_SYMBOL = "symbol";
    String ext;
    LineChart chart;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_activity);
        chart = (LineChart) findViewById(R.id.chart);
        chart.setBackgroundColor(getResources().getColor(R.color.white));
        chart.setDrawGridBackground(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.setPinchZoom(true);
        XAxis up = chart.getXAxis();
        up.setDrawAxisLine(true);
        up.setDrawLabels(true);
        up.setLabelCount(4);
        up.setValueFormatter(new MyYAxisValueFormatter());
        Intent graphIntent = getIntent();
        ext = graphIntent.getStringExtra("symbol");
        Description chartDescription = new Description();
        chartDescription.setTextSize(16.0f);
        chartDescription.setText(ext);
        chartDescription.setXOffset(16f);
        chart.setDescription(chartDescription);

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://query.yahooapis.com/").addConverterFactory(GsonConverterFactory.create()).build();
        HistoricalDataInterface dataInterface =  retrofit.create(HistoricalDataInterface.class);
        Date date = new Date();
        String q = "select * from yahoo.finance.historicaldata where symbol = \""+ ext +"\" and startDate = \""+Utility.get1WeekBackDate(date)+"\" and endDate = \""+ Utility.getFormattedDate(System.currentTimeMillis())+"\"";
        String diagnostics = "true";
        String env = "store://datatables.org/alltableswithkeys";
        String format = "json";
        Call<HistoricalDataPojo> call = dataInterface.getHistoricalData(q, diagnostics, env, format);
        call.enqueue(this);
    }

    @Override
    public void onResponse(Response<HistoricalDataPojo> response, Retrofit retrofit) {
        response.body();
        if(response.body()==null){
            return;
        }
        List<Entry> entries = new ArrayList<Entry>();
        entries = getEntries(response.body().getQuery().getResults().getQuote());
        if(entries!=null){
            Collections.sort(entries, new EntryXComparator());
            LineDataSet dataSet = new LineDataSet(entries, getResources().getString(R.string.graph_description));
            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.notifyDataSetChanged();
            dataSet.setDrawHighlightIndicators(true);
            dataSet.setCircleColor(Color.BLUE);
                       chart.invalidate();
        }
    }

    private List<Entry> getEntries(Quote[] quote) {
        List<Entry> entryList = new ArrayList<Entry>();
        for(Quote q : quote){
            long dateTimeMillis = getDateTimeMillis(q.getDate());
            Float f = Float.valueOf(dateTimeMillis);
            Entry entry = new Entry(f, Float.parseFloat(q.getClose()));
            entryList.add(entry);
        }
        return entryList;
    }

    private long getDateTimeMillis(String sDate) {
        Date date = new Date();
        SimpleDateFormat dtfOut = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = dtfOut.parse(sDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    @Override
    public void onFailure(Throwable t) {
    }

}
