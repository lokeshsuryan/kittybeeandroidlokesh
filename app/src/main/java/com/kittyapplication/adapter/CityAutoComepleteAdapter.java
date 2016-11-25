package com.kittyapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.utils.AppLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 8/10/16.
 */

public class CityAutoComepleteAdapter extends BaseAdapter implements Filterable {
    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<String> mListCity;
    private List<String> mListClone = new ArrayList<>();

    public CityAutoComepleteAdapter(Context context, List<String> list) {
        mContext = context;
        mListCity = list;
        mListClone.addAll(list);

        AppLog.e("TAG", "list size = " + mListCity.size());
    }

    @Override
    public int getCount() {
        return mListCity.size();
    }

    @Override
    public String getItem(int index) {
        return mListCity.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_layout_city, null);
        }

        try {
            ((TextView) convertView.findViewById(R.id.txtCity)).
                    setText(mListCity.get(position));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    final List<String> newList = new ArrayList<>();

                    for (int i = 0; i < mListClone.size(); i++) {
                        if (mListClone.get(i).toLowerCase().startsWith(constraint.toString().toLowerCase()))
                            newList.add(mListClone.get(i));
                    }

                    if (newList.isEmpty())
                        newList.addAll(mListClone);

                    results.values = newList;
                    results.count = newList.size();
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mListCity = (ArrayList<String>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }
}