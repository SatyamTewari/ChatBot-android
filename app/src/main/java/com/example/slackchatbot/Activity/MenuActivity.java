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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slackchatbot.Adapter.ChannelListAdapter;
import com.example.slackchatbot.Adapter.DmListAdapter;
import com.example.slackchatbot.Adapter.ScheduledMessagesAdapter;
import com.example.slackchatbot.Class.SlackApiClass;
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

    TextView addChannel;

    OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();
    Retrofit retrofit = new Retrofit.Builder().baseUrl(SlackApiClass.BASE_URL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build();
    private SlackApiClass retrofitCall = retrofit.create(SlackApiClass.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
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

        addChannel = findViewById(R.id.tv_add_channel);
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


        // add channel click action and open add channel dialog
        addChannel.setOnClickListener(it -> {
            Dialog dialog = new Dialog(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.add_channel_popup, null);
            dialog.setContentView(dialogView);
            Window window = dialog.getWindow();
            assert window != null;
            window.setGravity(Gravity.CENTER);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            EditText channelName = dialogView.findViewById(R.id.add_cahnnel_name_et);
            ImageView close = dialogView.findViewById(R.id.add_channel_close);
            CardView private_channel = dialogView.findViewById(R.id.private_channel);
            CardView public_channel = dialogView.findViewById(R.id.public_channel);

            close.setOnClickListener(d -> dialog.dismiss());

            // initiating private channel create click action
            private_channel.setOnClickListener(d -> {
                if(channelName.getText().toString().split(" ").length > 1){
                    CustomSnackBar.showSnackBar("Channel name cannot contain space", MenuActivity.this);
                }
                else if(!channelName.getText().toString().isEmpty()){
                    try {
                        dialog.dismiss();
                        Call<SuccessResponse> call = retrofitCall.createChannel(USER_TOKEN, channelName.getText().toString(), "true");
                        call.enqueue(new Callback<SuccessResponse>() {
                            @Override
                            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                                if (response.isSuccessful()) {
                                    getChannelsAndDmList("channel");
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

            // initiating public channel create click action
            public_channel.setOnClickListener(d -> {
                if(channelName.getText().toString().split(" ").length > 1){
                    CustomSnackBar.showSnackBar("Channel name cannot contain space", MenuActivity.this);
                }
                else if(!channelName.getText().toString().isEmpty()){
                    try {
                        dialog.dismiss();
                        Call<SuccessResponse> call = retrofitCall.createChannel(USER_TOKEN, channelName.getText().toString(), "false");
                        call.enqueue(new Callback<SuccessResponse>() {
                            @Override
                            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                                if (response.isSuccessful()) {
                                    getChannelsAndDmList("channel");
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
        getChannelsAndDmList("channel");
        getChannelsAndDmList("dm");
        getScheduledMsg("user");
        getScheduledMsg("bot");
    }

    // load the channel and Dm list
    private void getChannelsAndDmList(String channelOrDm) {
        Map<String, String> data = new HashMap<>();
        if(channelOrDm.equals("channel")){
            channelsProgressBar.setVisibility(View.VISIBLE);
            channelsRecyclerView.setVisibility(View.GONE);
            data.put("types", "public_channel,private_channel");
        }
        else{
            messagesProgressBar.setVisibility(View.VISIBLE);
            messagesRecyclerView.setVisibility(View.GONE);
            data.put("types", "mpim,im");
        }
        try {
            Call<ChannelsAPI> call = retrofitCall.getChannelAndDmList(USER_TOKEN, data);
            call.enqueue(new Callback<ChannelsAPI>() {
                @Override
                public void onResponse(Call<ChannelsAPI> call, Response<ChannelsAPI> response) {
                    channelsProgressBar.setVisibility(View.GONE);
                    messagesProgressBar.setVisibility(View.GONE);
                    if(channelOrDm.equals("channel")) {
                        channelsRecyclerView.setVisibility(View.VISIBLE);
                        if (response.isSuccessful()) {
                            channelsData = response.body();
                            channelsRecyclerView.setAdapter(new ChannelListAdapter(channelsData.getChannels(), MenuActivity.this));
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
                                    //blank for future use
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
                    else{
                        messagesRecyclerView.setVisibility(View.VISIBLE);
                        if (response.isSuccessful()) {
                            messagesData = response.body();
                            messagesRecyclerView.setAdapter(new DmListAdapter(messagesData.getChannels(), MenuActivity.this, retrofitCall));
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
                                    // blank for future use
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
                }

                @Override
                public void onFailure(Call<ChannelsAPI> call, Throwable t) {
                    Log.e(TAG, t.getMessage());
                    channelsProgressBar.setVisibility(View.GONE);
                    messagesProgressBar.setVisibility(View.GONE);
                }
            });
        }
        catch (Exception e){
            Log.e(TAG,"loadChannelsData exception: "+e.getMessage());
            channelsProgressBar.setVisibility(View.GONE);
            messagesProgressBar.setVisibility(View.GONE);
        }
    }

    // load the schedule Msg list for user and chatbot
    private void getScheduledMsg(String userType){
        try {
            Call<ScheduledMessagesAPI> call;
            if(userType.equals("user")) {
                scheduledUser.setVisibility(View.GONE);
                scheduledUserProgressBar.setVisibility(View.VISIBLE);
                call = retrofitCall.scheduledMessages(USER_TOKEN);
            }
            else {
                scheduledBot.setVisibility(View.GONE);
                scheduledBotProgressBar.setVisibility(View.VISIBLE);
                call = retrofitCall.scheduledMessages(BOT_TOKEN);
            }

            call.enqueue(new Callback<ScheduledMessagesAPI>() {
                @Override
                public void onResponse(Call<ScheduledMessagesAPI> call, Response<ScheduledMessagesAPI> response) {
                    if(userType.equals("user")) {
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
                    else{
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
                }

                @Override
                public void onFailure(Call<ScheduledMessagesAPI> call, Throwable t) {
                    Log.e(TAG,"loadScheduledUser failed: "+t.getMessage());
                    scheduledUserProgressBar.setVisibility(View.GONE);
                    scheduledBotProgressBar.setVisibility(View.GONE);
                }
            });
        }
        catch (Exception e){
            Log.e(TAG,"loadScheduledUser exception: "+e.getMessage());
            scheduledUserProgressBar.setVisibility(View.GONE);
            scheduledBotProgressBar.setVisibility(View.GONE);
        }
    }
}