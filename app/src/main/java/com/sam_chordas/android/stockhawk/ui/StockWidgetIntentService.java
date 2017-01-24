package com.sam_chordas.android.stockhawk.ui;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import static android.os.Binder.clearCallingIdentity;
import static android.os.Binder.restoreCallingIdentity;


/**
 * Created by DELL on 22-01-2017.
 */

public class StockWidgetIntentService extends RemoteViewsService {

    private Cursor mCursor;
    private Context mContext;
    int mWidgetid;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        mContext = getApplicationContext();
        mWidgetid = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);;
        return new RemoteViewsFactory() {
            private Cursor mData = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (mData != null) {
                    mData.close();
                }

                final long identityToken = clearCallingIdentity();

                final String[] projection = {
                        QuoteColumns._ID,
                        QuoteColumns.SYMBOL,
                        QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE,
                        QuoteColumns.CHANGE,
                        QuoteColumns.ISUP,
                        QuoteColumns.ISCURRENT
                };

                final String selection = QuoteColumns.ISCURRENT + "=?";
                final String[] selectionArgs = {"1"};
                final String orderBy = QuoteColumns._ID;

                mData = getContentResolver()
                        .query(QuoteProvider.Quotes.CONTENT_URI,
                                projection,
                                selection,
                                selectionArgs,
                                orderBy
                        );

                restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {
                if (mData != null) {
                    mData.close();
                    mData = null;
                }
                mContext = null;
            }

            @Override
            public int getCount() {
                if (mData == null) {
                    return 0;
                }
                return mData.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if(position == AdapterView.INVALID_POSITION || mData == null || !mData.moveToPosition(position)) {
                    return null;
                }

                final String symbol = mData.getString(mData.getColumnIndex(QuoteColumns.SYMBOL));
                final String bid = mData.getString(mData.getColumnIndex(QuoteColumns.BIDPRICE));
                final String percentChange = mData.getString(mData.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
                final boolean isUp = (mData.getInt(mData.getColumnIndex(QuoteColumns.ISUP)) == 1);

                final int textColor = (isUp)
                        ? R.color.material_green_700
                        : R.color.material_red_700;

                RemoteViews row = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);

                row.setTextViewText(R.id.stock_symbol, symbol);
                row.setTextViewText(R.id.bid_price, bid);
                row.setTextViewText(R.id.change, percentChange);
                row.setTextColor(R.id.change, ContextCompat.getColor(mContext, textColor));
                row.setTextColor(R.id.stock_symbol, ContextCompat.getColor(mContext, textColor));
                row.setTextColor(R.id.bid_price, ContextCompat.getColor(mContext, textColor));
                Intent intent = new Intent();
                intent.putExtra(MyStockDetailsActivity.STOCK_SYMBOL, symbol);
                row.setOnClickFillInIntent(R.id.stock_row, intent);
                return row;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (mData.moveToPosition(position)) {
                    return mData.getLong(mData.getColumnIndex(QuoteColumns._ID));
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
