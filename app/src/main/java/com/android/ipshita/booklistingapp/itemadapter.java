package com.android.ipshita.booklistingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ipshita on 26-09-2016.
 */
public class itemadapter extends ArrayAdapter<item> {

    public itemadapter(Context context, ArrayList<item> items){
        super( context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

// Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }


        item current = getItem(position);
        ViewHolder holder = new ViewHolder();
        holder.bookTitle = (TextView) listItemView.findViewById(R.id.title);
        holder.author = (TextView) listItemView.findViewById(R.id.author);
        listItemView.setTag(holder);

        holder.bookTitle.setText(current.gettitle());
        holder.author.setText(current.getauthor());


        return listItemView;

    }

    static class ViewHolder {
        TextView bookTitle;
        TextView author;
    }
}
