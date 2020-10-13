package com.example.slackchatbot.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slackchatbot.Models.ChannelsAPI.Channel;
import com.example.slackchatbot.R;

import java.util.List;


public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.MyViewHolder> {

    List<Channel> list;
    Context context;

    public ChannelListAdapter(List<Channel> listData, Context context) {
        this.list = listData;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channels_adapter_layout, parent, false);
        return new MyViewHolder((view));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(list.get(position).getName());
        if(list.get(position).getIsPrivate()){
            holder.channelPrivate.setImageResource(R.drawable.private_channel);
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView channelPrivate;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.channels_adapter_name_tv);
            channelPrivate = itemView.findViewById(R.id.channels_adapter_prefix);
        }
    }
}
