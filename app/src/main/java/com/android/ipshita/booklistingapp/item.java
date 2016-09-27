package com.android.ipshita.booklistingapp;

import org.json.JSONArray;

/**
 * Created by Ipshita on 26-09-2016.
 */
public class item {

    private String mtitle;
    private JSONArray mauthor;

    public item(String title, JSONArray author)
    {
        mtitle=title;
        mauthor=author;
    }

    public String gettitle(){return mtitle;}
    public JSONArray getauthor(){return mauthor;}
}
