package com.kittyapplication.core.ui.adapter;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by MIT on 10/21/2016.
 */
public abstract class BaseSelectedRecyclerViewAdapter<T> extends BaseRecyclerViewAdapter<T> {
    protected List<T> selectedItems;

    public BaseSelectedRecyclerViewAdapter(Context context, List<T> objectsList) {
        super(context, objectsList);
        selectedItems = new ArrayList<>();
    }

    public void toggleSelection(int position) {
        T item = getItem(position);
        toggleSelection(item);
    }

    public void toggleSelection(T item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
        notifyDataSetChanged();
    }

    public void removeSelection(T item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        }
        notifyDataSetChanged();
    }

    public void selectItem(int position) {
        T item = getItem(position);
        selectItem(item);
    }

    public void selectItem(T item) {
        if (selectedItems.contains(item)) {
            return;
        }
        selectedItems.add(item);
        notifyDataSetChanged();
    }

    public Collection<T> getSelectedItems() {
        return selectedItems;
    }

    protected boolean isItemSelected(int position) {
        return !selectedItems.isEmpty() && selectedItems.contains(getItem(position));
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }
}
