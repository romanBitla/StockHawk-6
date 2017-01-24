package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {
  Handler mHandler;
  public static final String ACTION_DATA_UPDATED = "com.sam_chordas.android.stockhawk.ACTION_DATA_UPDATED";
  public StockIntentService(){
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
    mHandler = new Handler();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    mHandler = new Handler();
    return super.onStartCommand(intent, flags, startId);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra("tag").equals("add")){
      args.putString("symbol", intent.getStringExtra("symbol"));
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    TaskParams taskParams = new TaskParams(intent.getStringExtra("tag"), args);
    int result = stockTaskService.onRunTask(taskParams);
    updateWidgets();
    if(result==2){
     mHandler.post(new Runnable() {
       @Override
       public void run() {
         Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_input), Toast.LENGTH_LONG).show();
       }
     });
    }
  }

  private void updateWidgets(){
    Context context = getApplicationContext();
    Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(context.getPackageName());
    context.sendBroadcast(dataUpdatedIntent);
  }

}
