package com.sam_chordas.android.stockhawk.retrointerface;

import com.sam_chordas.android.stockhawk.jsonpojo.HistoricalDataPojo;

import retrofit.Call;
import retrofit.http.GET;


/**
 * Created by Vidhya on 13-11-2016.
 */

public interface HistoricalDataInterface {

    @GET("v1/public/yql")
    Call<HistoricalDataPojo> getHistoricalData(@retrofit.http.Query("q") String q, @retrofit.http.Query("diagnostics") String diagnostics,
                                               @retrofit.http.Query("env") String env, @retrofit.http.Query("format") String format
    );
}
