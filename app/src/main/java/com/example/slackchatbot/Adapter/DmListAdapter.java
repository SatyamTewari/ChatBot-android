package com.example.slackchatbot.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.slackchatbot.Activity.MenuActivity;
import com.example.slackchatbot.Class.ApiRequestClass;
import com.example.slackchatbot.Models.ChannelsAPI.Channel;
import com.example.slackchatbot.Models.UserProfileAPI.UserProfileAPI;
import com.example.slackchatbot.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DmListAdapter extends RecyclerView.Adapter<DmListAdapter.MyViewHolder> {

    List<Channel> list;
    Context context;
    ApiRequestClass retrofitCall;

    public DmListAdapter(List<Channel> listData, Context context, ApiRequestClass call){
        this.list = listData;
        this.context = context;
        this.retrofitCall = call;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channels_adapter_layout,parent,false);
        return new MyViewHolder((view));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Call<UserProfileAPI> call = retrofitCall.userProfile(MenuActivity.BOT_TOKEN,list.get(position).getUser());
        call.enqueue(new Callback<UserProfileAPI>() {
            @Override
            public void onResponse(Call<UserProfileAPI> call, Response<UserProfileAPI> response) {
                if(response.isSuccessful()){
                    holder.name.setText(response.body().getUser().getName());
                    holder.image.setVisibility(View.VISIBLE);
                    holder.prefix.setVisibility(View.GONE);
                    MenuActivity.usersNames.put(list.get(position).getUser(),response.body().getUser().getName());
                    Glide.with(context).load(response.body().getUser().getProfile().getImage48()).into(holder.image);
                }else{
                    holder.name.setText(list.get(position).getUser());
                }
            }

            @Override
            public void onFailure(Call<UserProfileAPI> call, Throwable t) {
                holder.name.setText(list.get(position).getUser());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image,prefix;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.channels_adapter_name_tv);
            prefix = itemView.findViewById(R.id.channels_adapter_prefix);
            image = itemView.findViewById(R.id.channels_adapter_icon);
        }
    }
}
