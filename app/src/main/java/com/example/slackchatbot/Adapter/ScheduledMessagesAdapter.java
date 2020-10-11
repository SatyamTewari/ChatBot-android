package com.example.slackchatbot.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slackchatbot.Models.SchedulesMessagesAPI.ScheduledMessage;
import com.example.slackchatbot.R;

import java.util.List;

public class ScheduledMessagesAdapter extends RecyclerView.Adapter<ScheduledMessagesAdapter.MyViewHolder> {

    List<ScheduledMessage> list;
    Context context;

    public ScheduledMessagesAdapter(Context context,List<ScheduledMessage> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.scheduled_messages_adapter_layout,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(list.get(position).getText());
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.scheduled_name);
        }
    }
}
