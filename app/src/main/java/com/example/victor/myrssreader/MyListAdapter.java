package com.example.victor.myrssreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MyListAdapter extends BaseAdapter {
    LayoutInflater inflater;
    ArrayList<ReaderModel> model;

    public MyListAdapter(Context context, ArrayList<ReaderModel> model) {
        this.model = model;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return model.size();
    }

    @Override
    public ReaderModel getItem(int position) {
        return model.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_briefly, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.text_rss);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String str="";
        str=getItem(position).getTitle()+"\n"+getItem(position).getAuthor()+getItem(position).getDate();
        viewHolder.tvTitle.setText(str);
        return convertView;
    }

    static class ViewHolder {
        TextView tvTitle;
    }
}
/*models.add(new ReaderModel(R.drawable.pina, "pina", "colada"));
        models.add(new ReaderModel(R.drawable.portoflip, "porto", "flip"));
        models.add(new ReaderModel(R.drawable.tequilasunrise, "tequila", "sunrise"));*/

        /*listView = (ListView) findViewById(R.id.list_reader);
        MyListAdapter listAdapter = new MyListAdapter(this, models);
        listView.setAdapter(listAdapter);*/