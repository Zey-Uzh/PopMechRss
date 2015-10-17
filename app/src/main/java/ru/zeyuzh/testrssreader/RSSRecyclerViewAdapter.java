package ru.zeyuzh.testrssreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RSSRecyclerViewAdapter extends RecyclerView.Adapter<RSSRecyclerViewAdapter.RSSViewHolder> {
    Cursor cursor;
    Context mContext;

    //constructor
    public RSSRecyclerViewAdapter(Cursor cursor, Context mContext) {
        this.cursor = cursor;
        this.mContext=mContext;
    }

    //preparing ViewHolder
    public static class RSSViewHolder extends RecyclerView.ViewHolder {
        TextView tvElementTitle;
        TextView tvElementDescription;
        TextView tvElementDate;

        RSSViewHolder(View itemView) {
            super(itemView);
            tvElementTitle = (TextView) itemView.findViewById(R.id.tvElementTitle);
            tvElementDescription = (TextView) itemView.findViewById(R.id.tvElementDescription);
            tvElementDate = (TextView) itemView.findViewById(R.id.tvElementDate);
        }
    }

    @Override
    public RSSRecyclerViewAdapter.RSSViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_of_listview, viewGroup, false);
        RSSViewHolder rssViewHolder = new RSSViewHolder(v);
        return rssViewHolder;
    }

    @Override
    public void onBindViewHolder(RSSRecyclerViewAdapter.RSSViewHolder viewHolder, int i) {
        cursor.moveToPosition(i);
        viewHolder.tvElementTitle.setText(cursor.getString(cursor.getColumnIndex(RSSContentProvider.COLUMN_NAME_TITLE)));
        viewHolder.tvElementDescription.setText(cursor.getString(cursor.getColumnIndex(RSSContentProvider.COLUMN_NAME_DESCRIPTION)));
        viewHolder.tvElementDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = cursor.getString(cursor.getColumnIndex(RSSContentProvider.COLUMN_NAME_LINK));
                Uri address = Uri.parse(link);
                Intent openlink = new Intent(Intent.ACTION_VIEW, address);
                mContext.startActivity(openlink);
            }
        });
        viewHolder.tvElementDate.setText(cursor.getString(cursor.getColumnIndex(RSSContentProvider.COLUMN_NAME_PUBDATE)));
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
