package com.kittyapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.model.NotesResponseDao;

import java.util.List;

/**
 * Created by Dhaval Riontech on 12/8/16.
 */
public class NotesListAdapter extends BaseAdapter {

    private List<NotesResponseDao> mList;
    private Context mContext;
    private static LayoutInflater inflater = null;

    public NotesListAdapter(Context context, List<NotesResponseDao> list) {
        mContext = context;
        mList = list;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_notes, parent, false);
            viewHolder = new Holder();
            viewHolder.txtNotesTitle = (TextView) convertView.findViewById(R.id.txtNotesTitle);
            viewHolder.txtNotesData = (TextView) convertView.findViewById(R.id.txtNotesData);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }

        viewHolder.txtNotesTitle.setText(mList.get(position).getTitle());
        viewHolder.txtNotesData.setText(mList.get(position).getDescription());
        return convertView;
    }

    public class Holder {
        private TextView txtNotesTitle;
        private TextView txtNotesData;
    }
}
