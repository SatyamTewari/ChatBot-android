package com.example.slackchatbot.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.example.slackchatbot.Adapter.ChannelsAdapter;
import com.example.slackchatbot.Adapter.DMAdapter;
import com.example.slackchatbot.Adapter.ScheduledMessagesAdapter;
import com.example.slackchatbot.Class.ApiRequestClass;
import com.example.slackchatbot.Class.CustomSnackBar;
import com.example.slackchatbot.Class.RecyclerItemClickListener;
import com.example.slackchatbot.Models.ChannelsAPI.ChannelsAPI;
import com.example.slackchatbot.Models.SchedulesMessagesAPI.ScheduledMessagesAPI;
import com.example.slackchatbot.Models.SuccessResponse;
import com.example.slackchatbot.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MenuActivity extends AppCompatActivity {

    private final String TAG = "MenuActivity";
    public static String BOT_TOKEN = "";
    public static String USER_TOKEN = "";
    public static Map<String,String> usersNames = new HashMap<>();

    RecyclerView channelsRecyclerView, messagesRecyclerView, scheduledUser, scheduledBot;
    ProgressBar channelsProgressBar, messagesProgressBar, scheduledUserProgressBar, scheduledBotProgressBar;
    ChannelsAPI channelsData;
    ChannelsAPI messagesData;
    ScheduledMessagesAPI userScheduled,botScheduled;

    boolean doubleBackToExitPressedOnce = false;

    MaterialRippleLayout addChannel;

    OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).build();
    Retrofit retrofit = new Retrofit.Builder().baseUrl(ApiRequestClass.BASE_URL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build();
    private ApiRequestClass retrofitCall = retrofit.create(ApiRequestClass.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        BOT_TOKEN = getString(R.string.bot_token);
        USER_TOKEN = getString(R.string.user_token);
        try {
            toolbar.setTitle("ChatBot");
            toolbar.setTitleTextColor(getColor(android.R.color.white));
            setSupportActionBar(toolbar);
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }

        addChannel = findViewById(R.id.main_channel_add);
        channelsRecyclerView = findViewById(R.id.main_channels_rv);
        messagesRecyclerView = findViewById(R.id.main_dm_rv);
        scheduledUser = findViewById(R.id.main_sch_user_rv);
        scheduledBot = findViewById(R.id.main_sch_bot_rv);

        channelsProgressBar = findViewById(R.id.main_channels_pb);
        messagesProgressBar = findViewById(R.id.main_dm_pb);
        scheduledUserProgressBar = findViewById(R.id.main_sch_user_pb);
        scheduledBotProgressBar = findViewById(R.id.main_sch_bot_pb);

        channelsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduledUser.setLayoutManager(new LinearLayoutManager(this));
        scheduledBot.setLayoutManager(new LinearLayoutManager(this));


        addChannel.setOnClickListener(it -> {
            Dialog dialog = new Dialog(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.add_channel_dialog, null);
            dialog.setContentView(dialogView);
            dialog.setCancelable(false);
            Window window = dialog.getWindow();
            assert window != null;
            window.setGravity(Gravity.CENTER);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            EditText channelName = dialogView.findViewById(R.id.add_cahnnel_name_et);
            SwitchCompat channelType = dialogView.findViewById(R.id.add_channel_channel_type_switch);
            ImageView close = dialogView.findViewById(R.id.add_channel_close);
            Button save = dialogView.findViewById(R.id.add_channel_btn);
            close.setOnClickListener(d -> dialog.dismiss());
            save.setOnClickListener(d -> {
                if(channelName.getText().toString().split(" ").length > 1){
                    CustomSnackBar.showSnackBar("Channel name cannot contain space", MenuActivity.this);
                }
                else if(!channelName.getText().toString().isEmpty()){
                    try {
                        dialog.dismiss();
                        Call<SuccessResponse> call = retrofitCall.createChannel(USER_TOKEN, channelName.getText().toString(), channelType.isChecked() ? "true" : "false");
                        call.enqueue(new Callback<SuccessResponse>() {
                            @Override
                            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                                if (response.isSuccessful()) {
                                    loadChannelsData();
                                }
                            }

                            @Override
                            public void onFailure(Call<SuccessResponse> call, Throwable t) {
                                Log.e(TAG,"addChannel failed : "+t.getMessage());
                            }
                        });
                    }
                    catch (Exception e){
                        Log.e(TAG,"addChannel exception : "+e.getMessage());
                    }
                }
            });
            dialog.show();
        });


        loadChannelsData();
        loadMessagesData();
        loadScheduledUser();
        loadScheduledBot();
    }

    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }
        CustomSnackBar.showSnackBar("Press Back again to exit", this);
        this.doubleBackToExitPressedOnce = true;
        new Handler().postDelayed(() -> {
            doubleBackToExitPressedOnce = false;
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChannelsData();
        loadMessagesData();
        loadScheduledUser();
        loadScheduledBot();
    }

    private void loadChannelsData() {
        channelsProgressBar.setVisibility(View.VISIBLE);
        channelsRecyclerView.setVisibility(View.GONE);
        Map<String, String> data = new HashMap<>();
        data.put("types", "public_channel,private_channel");
        try {
            Call<ChannelsAPI> call = retrofitCall.channels(USER_TOKEN, data);
            call.enqueue(new Callback<ChannelsAPI>() {
                @Override
                public void onResponse(Call<ChannelsAPI> call, Response<ChannelsAPI> response) {
                    channelsProgressBar.setVisibility(View.GONE);
                    channelsRecyclerView.setVisibility(View.VISIBLE);
                    if (response.isSuccessful()) {
                        channelsData = response.body();
                        channelsRecyclerView.setAdapter(new ChannelsAdapter(channelsData.getChannels(), MenuActivity.this));
                        channelsRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(MenuActivity.this, channelsRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Intent intent = new Intent(MenuActivity.this, MessagesActivity.class);
                                intent.putExtra("type", channelsData.getChannels().get(position).getIsChannel() ? "channel" : "group");
                                intent.putExtra("id", channelsData.getChannels().get(position).getId());
                                intent.putExtra("name", channelsData.getChannels().get(position).getName());
                                intent.putExtra("user", "");
                                startActivity(intent);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }
                        }));
                    } else {
                        try {
                            Log.e(TAG, response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ChannelsAPI> call, Throwable t) {
                    Log.e(TAG, t.getMessage());
                    channelsProgressBar.setVisibility(View.GONE);
                }
            });
        }
        catch (Exception e){
            Log.e(TAG,"loadChannelsData exception: "+e.getMessage());
            channelsProgressBar.setVisibility(View.GONE);
        }
    }

    private void loadMessagesData() {
        try {
            messagesProgressBar.setVisibility(View.VISIBLE);
            messagesRecyclerView.setVisibility(View.GONE);
            Map<String, String> data = new HashMap<>();
            data.put("types", "mpim,im");
            Call<ChannelsAPI> call = retrofitCall.channels(USER_TOKEN, data);
            call.enqueue(new Callback<ChannelsAPI>() {
                @Override
                public void onResponse(Call<ChannelsAPI> call, Response<ChannelsAPI> response) {
                    messagesProgressBar.setVisibility(View.GONE);
                    messagesRecyclerView.setVisibility(View.VISIBLE);
                    if (response.isSuccessful()) {
                        messagesData = response.body();
                        messagesRecyclerView.setAdapter(new DMAdapter(messagesData.getChannels(), MenuActivity.this, retrofitCall));
                        messagesRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(MenuActivity.this, messagesRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Intent intent = new Intent(MenuActivity.this, MessagesActivity.class);
                                intent.putExtra("type", "dm");
                                intent.putExtra("id", messagesData.getChannels().get(position).getId());
                                intent.putExtra("name", "");
                                intent.putExtra("user", messagesData.getChannels().get(position).getUser());
                                startActivity(intent);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }
                        }));
                    } else {
                        try {
                            Log.e(TAG, response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ChannelsAPI> call, Throwable t) {
                    Log.e(TAG, t.getMessage());
                    messagesProgressBar.setVisibility(View.GONE);
                }
            });
        }
        catch (Exception e){
            Log.e(TAG,"loadMessagesData exception: "+e.getMessage());
            messagesProgressBar.setVisibility(View.GONE);
        }
    }

    private void loadScheduledUser(){
        try {
            scheduledUser.setVisibility(View.GONE);
            scheduledUserProgressBar.setVisibility(View.VISIBLE);
            Call<ScheduledMessagesAPI> call = retrofitCall.scheduledMessages(USER_TOKEN);
            call.enqueue(new Callback<ScheduledMessagesAPI>() {
                @Override
                public void onResponse(Call<ScheduledMessagesAPI> call, Response<ScheduledMessagesAPI> response) {
                    scheduledUser.setVisibility(View.VISIBLE);
                    scheduledUserProgressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        try {
                            userScheduled = response.body();
                            scheduledUser.setAdapter(new ScheduledMessagesAdapter(MenuActivity.this, userScheduled.getScheduledMessages()));
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ScheduledMessagesAPI> call, Throwable t) {
                    Log.e(TAG,"loadScheduledUser failed: "+t.getMessage());
                    scheduledUserProgressBar.setVisibility(View.GONE);
                }
            });
        }
        catch (Exception e){
            Log.e(TAG,"loadScheduledUser exception: "+e.getMessage());
            scheduledUserProgressBar.setVisibility(View.GONE);
        }
    }

    private void loadScheduledBot(){
        try {
            scheduledBot.setVisibility(View.GONE);
            scheduledBotProgressBar.setVisibility(View.VISIBLE);
            Call<ScheduledMessagesAPI> call = retrofitCall.scheduledMessages(BOT_TOKEN);
            call.enqueue(new Callback<ScheduledMessagesAPI>() {
                @Override
                public void onResponse(Call<ScheduledMessagesAPI> call, Response<ScheduledMessagesAPI> response) {
                    scheduledBot.setVisibility(View.VISIBLE);
                    scheduledBotProgressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        try {
                            botScheduled = response.body();
                            scheduledBot.setAdapter(new ScheduledMessagesAdapter(MenuActivity.this, botScheduled.getScheduledMessages()));
                        } catch (Exception e) {
                            Log.e(TAG, "loadScheduledBot exception: "+e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ScheduledMessagesAPI> call, Throwable t) {
                    scheduledBotProgressBar.setVisibility(View.GONE);
                    Log.e(TAG, "loadScheduledBot failed: "+t.getMessage());
                }
            });
        }
        catch (Exception e){
            scheduledBotProgressBar.setVisibility(View.GONE);
            Log.e(TAG, "loadScheduledBot exception: "+e.getMessage());
        }
    }
}