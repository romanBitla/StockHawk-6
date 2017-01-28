package com.sam_chordas.android.stockhawk.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

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


public class MyStockDetailsActivity extends AppCompatActivity implements Callback<HistoricalDataPojo> {
    public static final String STOCK_SYMBOL = "symbol";
    private String mExt;
    private LineChart mChart;
    private ProgressDialog mProgressBar;
    private int mProgressBarStatus = 0;
    private static String DIAGNOSTICS = "true";
    private static String ENV = "store://datatables.org/alltableswithkeys";
    private static String FORMAT = "json";
    private  static HistoricalDataInterface mDataInterface;
    private static Retrofit mRetrofit;
    static {
        mRetrofit = new Retrofit.Builder().baseUrl("https://query.yahooapis.com/").addConverterFactory(GsonConverterFactory.create()).build();
        mDataInterface = mRetrofit.create(HistoricalDataInterface.class);
    }

    static final RadioGroup.OnCheckedChangeListener ToggleListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final RadioGroup radioGroup, final int i) {
            for (int j = 0; j < radioGroup.getChildCount(); j++) {
                final ToggleButton view = (ToggleButton) radioGroup.getChildAt(j);
                view.setChecked(view.getId() == i);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_activity);
        ((RadioGroup) findViewById(R.id.toggle_group)).setOnCheckedChangeListener(ToggleListener);
        mChart = (LineChart) findViewById(R.id.chart);
        mChart.setBackgroundColor(getResources().getColor(R.color.white));
        mChart.setDrawGridBackground(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.setNoDataText("");
        mChart.getAxisRight().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getAxisLeft().setDrawLabels(false);
        mChart.getXAxis().setLabelCount(4,true);
        mChart.getXAxis().setAvoidFirstLastClipping(true);
        mChart.setPinchZoom(false);
        mChart.setScaleXEnabled(false);
        mChart.setScaleYEnabled(false);
        mChart.setClickable(false);
        XAxis up = mChart.getXAxis();
        mChart.getAxisLeft().setDrawAxisLine(false);
        up.setDrawAxisLine(true);
        up.setDrawLabels(true);
        up.setValueFormatter(new MyYAxisValueFormatter());
        Intent graphIntent = getIntent();
        mExt = graphIntent.getStringExtra("symbol");
        getSupportActionBar().setTitle(mExt);
        Description chartDescription = new Description();
        chartDescription.setTextSize(16.0f);
        chartDescription.setText(mExt);
        chartDescription.setXOffset(16f);
        mChart.setDescription(chartDescription);
        findViewById(R.id.six_months).performClick();
    }

    @Override
    public void onResponse(Response<HistoricalDataPojo> response, Retrofit retrofit) {
        response.body();
        if (response.body() == null || response.body().getQuery().getResults() == null) {
            mChart.setNoDataText(getResources().getString(R.string.no_graph_data));
            mChart.invalidate();
            mProgressBar.dismiss();
            return;
        }
        List<Entry> entries = new ArrayList<Entry>();
        entries = getEntries(response.body().getQuery().getResults().getQuote());
        if (entries != null) {
            Collections.sort(entries, new EntryXComparator());
            LineDataSet dataSet = new LineDataSet(entries, getResources().getString(R.string.graph_description));
            LineData lineData = new LineData(dataSet);
            mChart.setData(lineData);
            mChart.notifyDataSetChanged();
            dataSet.setDrawCircleHole(false);
            dataSet.setCircleColor(Color.BLUE);
            dataSet.setCircleRadius(2f);
            dataSet.setDrawHighlightIndicators(false);
            mChart.invalidate();
            mProgressBar.dismiss();
        }
    }

    private List<Entry> getEntries(Quote[] quote) {
        List<Entry> entryList = new ArrayList<Entry>();
        for (Quote q : quote) {
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
        mChart.setNoDataText(getResources().getString(R.string.no_graph_data));
        mProgressBar.dismiss();
    }


    public void onToggle(View view) {

        ((RadioGroup) view.getParent()).check(view.getId());
        mProgressBar = new ProgressDialog(this);
        mProgressBar.setCancelable(true);
        mProgressBar.setMessage(getResources().getString(R.string.loading_message));
        mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressBar.setProgress(0);
        mProgressBar.setMax(100);
        mProgressBar.show();
        mProgressBarStatus = 0;
        switch (view.getId()) {
            case R.id.one_week: {
                Date date = new Date();
                String q = "select * from yahoo.finance.historicaldata where symbol = \"" + mExt + "\" and startDate = \"" + Utility.getOneWeekBackDate(date) + "\" and endDate = \"" + Utility.getFormattedDate(System.currentTimeMillis()) + "\"";
                Call<HistoricalDataPojo> call = mDataInterface.getHistoricalData(q, DIAGNOSTICS, ENV, FORMAT);
                call.enqueue(this);
                break;
            }
            case R.id.one_year: {
                Date date = new Date();
                String q = "select * from yahoo.finance.historicaldata where symbol = \"" + mExt + "\" and startDate = \"" + Utility.getOneYearBackDate(date) + "\" and endDate = \"" + Utility.getFormattedDate(System.currentTimeMillis()) + "\"";
                Call<HistoricalDataPojo> call = mDataInterface.getHistoricalData(q, DIAGNOSTICS, ENV, FORMAT);
                call.enqueue(this);
                break;
            }
            case R.id.six_months: {
                Date date = new Date();
                String q = "select * from yahoo.finance.historicaldata where symbol = \"" + mExt + "\" and startDate = \"" + Utility.getSixMonthBackDate(date) + "\" and endDate = \"" + Utility.getFormattedDate(System.currentTimeMillis()) + "\"";
                Call<HistoricalDataPojo> call = mDataInterface.getHistoricalData(q, DIAGNOSTICS, ENV, FORMAT);
                call.enqueue(this);
                break;
            }
        }
    }
}
