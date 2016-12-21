package com.kittyapplication.adapter;

/**
 * Created by Iball on 12/21/2016.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kittyapplication.R;
import com.kittyapplication.model.PromotionalItemObject;

import java.util.List;

public class PromotionalRecylerAdapter extends RecyclerView.Adapter<PromotionalViewHolder> {

    private List<PromotionalItemObject> itemList;
    private Context context;

    public PromotionalRecylerAdapter(Context context, List<PromotionalItemObject> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public PromotionalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.permotional_layout_row, null);
        PromotionalViewHolder rcv = new PromotionalViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(PromotionalViewHolder holder, int position) {
        holder.countryName.setText(itemList.get(position).getName());
        holder.countryPhoto.setImageResource(itemList.get(position).getPhoto());
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }
}
