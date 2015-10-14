package ru.zeyuzh.testrssreader;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class RSSBaseAdapter extends BaseAdapter {

    private Context context;
    private List<RSSMessage> entries = new ArrayList<RSSMessage>();
    LayoutInflater inflater;

    public RSSBaseAdapter(Context context, List<RSSMessage> entries) {
        this.context = context;
        this.entries = entries;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Log.d("lg", "Start adding another view in ListView");
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.element_of_listview, parent, false);
        }

        TextView tvElementTitle = (TextView) view.findViewById(R.id.tvElementTitle);
        TextView tvElementDescription = (TextView) view.findViewById(R.id.tvElementDescription);
        TextView tvElementDate = (TextView) view.findViewById(R.id.tvElementDate);

        tvElementTitle.setText(entries.get(position).getTitle());
        tvElementDescription.setText(entries.get(position).getDescription());
        tvElementDate.setText(entries.get(position).getPubDate());

        return view;
    }
}
