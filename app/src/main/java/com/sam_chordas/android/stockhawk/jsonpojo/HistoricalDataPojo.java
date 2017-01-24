package com.sam_chordas.android.stockhawk.jsonpojo;

/**
 * Created by Vidhya on 14-11-2016.
 */


public class HistoricalDataPojo
{
    private Query query;

    public Query getQuery ()
    {
        return query;
    }

    public void setQuery (Query query)
    {
        this.query = query;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [query = "+query+"]";
    }
}