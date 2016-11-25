package com.kittyapplication.core.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.kittyapplication.core.ui.listener.PaginationHistoryListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MIT on 10/21/2016.
 */
public abstract class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected LayoutInflater inflater;
    protected Context context;
    protected List<T> objectsList;
    protected PaginationHistoryListener paginationHistoryListener;
    private int previousGetCount = 0;

    public BaseRecyclerViewAdapter(Context context) {
        this(context, new ArrayList<T>());
    }

    public BaseRecyclerViewAdapter(Context context, List<T> objectsList) {
        this.context = context;
        this.objectsList = objectsList;
        this.inflater = LayoutInflater.from(context);
    }

    public void setPaginationHistoryListener(PaginationHistoryListener paginationHistoryListener) {
        this.paginationHistoryListener = paginationHistoryListener;
    }

    @Override
    public int getItemCount() {
        return objectsList.size();
    }

    public int getCount() {
        return objectsList.size();
    }

    public T getItem(int position) {
        return objectsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateList(List<T> newData) {
        objectsList = newData;
        notifyDataSetChanged();
    }

    public void add(T item) {
        objectsList.add(item);
        notifyDataSetChanged();
    }

    public void addAt(int position, T item) {
        objectsList.add(position, item);
        notifyItemInserted(position);
        notifyDataSetChanged();
    }

    public void addList(List<T> items) {
        objectsList.addAll(0, items);
        notifyItemInserted(0);
        notifyDataSetChanged();
    }

    public void addAtLast(List<T> items) {
        objectsList.addAll(items);
        notifyDataSetChanged();
    }

    public List<T> getList() {
        return objectsList;
    }

    public void remove(T item) {
        objectsList.remove(item);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        objectsList.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    protected void downloadMore(int position) {
        try {
            if (position == 0) {
                if (paginationHistoryListener != null) {
                    if (getItemCount() != previousGetCount) {
                        paginationHistoryListener.downloadMore();
                        previousGetCount = getItemCount();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
