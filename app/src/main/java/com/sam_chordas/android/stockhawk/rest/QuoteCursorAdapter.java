package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;

import static com.sam_chordas.android.stockhawk.service.StockIntentService.ACTION_DATA_UPDATED;

/**
 * Created by sam_chordas on 10/6/15.
 * Credit to skyfishjy gist:
 * https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private static Context mContext;
    private static Typeface robotoLight;
    private boolean isPercent;

    public QuoteCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_quote, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
        String stockSymbol =  cursor.getString(cursor.getColumnIndex("symbol"));
        viewHolder.symbol.setText(stockSymbol);
        viewHolder.symbol.setContentDescription(mContext.getResources().getString(R.string.stock_symbol)+" "+stockSymbol);
        String bidPrice = cursor.getString(cursor.getColumnIndex("bid_price"));
        viewHolder.bidPrice.setText(bidPrice);
        viewHolder.bidPrice.setContentDescription(mContext.getResources().getString(R.string.bid_price)+bidPrice);
        int sdk = Build.VERSION.SDK_INT;
        if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.change.setBackgroundDrawable(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            } else {
                viewHolder.change.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            }
        } else {
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.change.setBackgroundDrawable(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            } else {
                viewHolder.change.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            }
        }
        if (Utils.showPercent) {
            String precentageChange = cursor.getString(cursor.getColumnIndex("percent_change"));
            viewHolder.change.setText(precentageChange);
            viewHolder.change.setContentDescription(mContext.getResources().getString(R.string.percentage_change) + precentageChange);
        } else {
            String stockValueChange = cursor.getString(cursor.getColumnIndex("change"));
            viewHolder.change.setText(stockValueChange);
            viewHolder.change.setContentDescription(mContext.getResources().getString(R.string.value_change) + stockValueChange);
        }
    }

    @Override
    public void onItemDismiss(int position) {
        Cursor c = getCursor();
        c.moveToPosition(position);
        String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
        mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);
        notifyItemRemoved(position);
        updateWidgets();
    }
    private void updateWidgets(){
        Context context = mContext;
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperViewHolder, View.OnClickListener {
        public final TextView symbol;
        public final TextView bidPrice;
        public final TextView change;

        public ViewHolder(View itemView) {
            super(itemView);
            symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
            symbol.setTypeface(robotoLight);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setBackground(mContext.getDrawable(R.drawable.list_item_selector));
            }
          /*  int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray typedArray = mContext.obtainStyledAttributes(attrs);
            int backgroundResource = typedArray.getResourceId(0, 0);
            itemView.setBackgroundResource(backgroundResource);*/
            itemView.setClickable(true);
            bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
            change = (TextView) itemView.findViewById(R.id.change);
        }

        @Override
        public void onItemSelected() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                itemView.setBackgroundColor(mContext.getColor(R.color.white));
            }
        }

        @Override
        public void onItemClear() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                itemView.setBackgroundColor(mContext.getColor(R.color.white));
            }
        }

        @Override
        public void onClick(View v) {

        }
    }
}
