package com.android.woundmonitoringsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WoundRecyclerAdapter extends RecyclerView.Adapter<WoundRecyclerAdapter.Holder> implements View.OnClickListener {
    ArrayList<WoundList> list;
    WoundRecyclerAdapter(ArrayList<WoundList> list){
        this.list = list;

    }

    @Override
    public void onClick(View view) {

    }

    public static class Holder extends RecyclerView.ViewHolder{
        TextView type , color , size;
        public Holder(@NonNull View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.type);
            color = itemView.findViewById(R.id.color);
            size =  itemView.findViewById(R.id.size);
        }
    }
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wounds_list,parent , false);
        Holder holder = new Holder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.type.setText(list.get(position).getType());
        holder.color.setText(list.get(position).getColor());
        holder.size.setText(list.get(position).getSize());
//        holder.name.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

