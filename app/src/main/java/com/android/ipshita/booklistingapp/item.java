package com.android.ipshita.booklistingapp;

/**
 * Created by Ipshita on 26-09-2016.
 */
public class item {

    private String mtitle;
    private String mauthor;

    public item(String title, String author)
    {
        mtitle=title;
        mauthor=author;
    }

    public String gettitle(){return mtitle;}
    public String getauthor(){return mauthor;}
}
