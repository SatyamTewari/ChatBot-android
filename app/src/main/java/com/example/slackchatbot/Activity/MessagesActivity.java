package com.example.slackchatbot.Activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.example.slackchatbot.Adapter.ChannelMessagesAdapter;
import com.example.slackchatbot.Adapter.DMMessagesAdapter;
import com.example.slackchatbot.Adapter.ScheduleArrayAdapter;
import com.example.slackchatbot.Class.ApiRequestClass;
import com.example.slackchatbot.Class.CustomSnackBar;
import com.example.slackchatbot.Class.RecyclerItemClickListener;
import com.example.slackchatbot.Models.ChannelInfoAPI.ChannelInfoAPI;
import com.example.slackchatbot.Models.ChannelJoinAPI.ChannelJoinAPI;
import com.example.slackchatbot.Models.ChannelMessagesAPI.ChannelMessagesAPI;
import com.example.slackchatbot.Models.DMMessagesAPI.DMMessagesAPI;
import com.example.slackchatbot.Models.DelMessageAPI;
import com.example.slackchatbot.Models.SuccessResponse;
import com.example.slackchatbot.Models.UserProfileAPI.UserProfileAPI;
import com.example.slackchatbot.R;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.slackchatbot.Activity.MenuActivity.BOT_TOKEN;
import static com.example.slackchatbot.Activity.MenuActivity.USER_TOKEN;

public class MessagesActivity extends AppCompatActivity {

    private final String TAG = "MessagesActivity";

    private final String[] SCHEDULES = {"Once", "Every 7 days", "Every 30 days"};

    OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).build();
    Retrofit retrofit = new Retrofit.Builder().baseUrl(ApiRequestClass.BASE_URL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build();
    private ApiRequestClass retrofitCall = retrofit.create(ApiRequestClass.class);

    DMMessagesAPI userMessages;
    ChannelMessagesAPI channelMessages;

    RecyclerView messagesRV;
    MaterialRippleLayout backBtn, timer;
    TextView title;
    Dialog dialog;

    SharedPreferences prefs;

    ImageView sendBtn, sendBotBtn;
    EditText typedMessage;

    LinearLayout holder;

    String type = "", id = "", name = "", user = "";

    ChannelInfoAPI userChannelInfo, botChannelInfo;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        Bundle extras = getIntent().getExtras();
        try {
            type = extras.getString("type", "");
            id = extras.getString("id", "");
            name = extras.getString("name", "");
            user = extras.getString("user", "");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        if (type.equals("") || id.equals("")) {
            CustomSnackBar.showSnackBar("Something went wrong",MessagesActivity.this);
            super.onBackPressed();
        }

        messagesRV = findViewById(R.id.messages_rv);
        backBtn = findViewById(R.id.messages_back);
        title = findViewById(R.id.messages_title);
        typedMessage = findViewById(R.id.messages_edit_text);
        sendBtn = findViewById(R.id.messages_send_btn);
        holder = findViewById(R.id.send_message_holder);
        sendBotBtn = findViewById(R.id.messages_send_bot_btn);
        timer = findViewById(R.id.messages_timer);

        sendBtn.setOnClickListener(it -> sendMessage());

        backBtn.setOnClickListener(it -> super.onBackPressed());

        timer.setOnClickListener(it -> {
            Dialog dialog = new Dialog(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.schedule_message_dialog, null);
            dialog.setContentView(dialogView);
            Window window = dialog.getWindow();
            assert window != null;
            window.setGravity(Gravity.CENTER);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            int[] selected = {0};
            EditText msg = dialogView.findViewById(R.id.schedule_message_et);
            CardView chatbot = dialogView.findViewById(R.id.schedule_chatbot);
            CardView user = dialogView.findViewById(R.id.schedule_user);
            Spinner scheduling = dialogView.findViewById(R.id.schedule_channels_spinner);
            TextView date = dialogView.findViewById(R.id.schedule_date);
            TextView time = dialogView.findViewById(R.id.schedule_time);

            Calendar calendar = Calendar.getInstance();

            date.setText(getDate(Calendar.getInstance().getTime()));
            time.setText(getTime(Calendar.getInstance().getTime()));

            int[] dateSelected = {calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)};
            int[] timeSelected = {calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)};

            date.setOnClickListener(d -> {
                new DatePickerDialog(MessagesActivity.this, (datePicker, year, month, day) -> {
                    date.setText(day + "-" + (month + 1) + "-" + year);
                    dateSelected[0] = day;
                    dateSelected[1] = month + 1;
                    dateSelected[2] = year;
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            });
            time.setOnClickListener(d -> {
                new TimePickerDialog(MessagesActivity.this, (timePicker, hour, minute) -> {
                    timeSelected[0] = hour;
                    timeSelected[1] = minute;
                    time.setText(hour + ":" + minute);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            });


            scheduling.setAdapter(new ScheduleArrayAdapter(MessagesActivity.this, R.layout.spinner_list_item, R.id.spinner_tv, SCHEDULES));
            scheduling.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selected[0] = i;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            user.setOnClickListener(d -> {
                if(!msg.getText().toString().trim().isEmpty()){
                    Long ts = (stringToDate(dateSelected[0] + "-" + dateSelected[1] + "-" + dateSelected[2] + " " + timeSelected[0] + "-" + timeSelected[1]).getTime());
                    if(userChannelInfo.getChannel().getIsMember()){
                        dialog.dismiss();
                        Call<SuccessResponse> call = retrofitCall.scheduleMessage(USER_TOKEN,id,msg.getText().toString().trim(),String.valueOf(ts/1000));
                        call.enqueue(new Callback<SuccessResponse>() {
                            @Override
                            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                                if(response.isSuccessful()){
                                    CustomSnackBar.showSnackBar("Message Scheduled Successfully",MessagesActivity.this);
                                }
                            }

                            @Override
                            public void onFailure(Call<SuccessResponse> call, Throwable t) {

                            }
                        });
                    }else{
                        CustomSnackBar.showSnackBar("Permission Denied! Not a member",MessagesActivity.this);
                    }
                }
            });

            chatbot.setOnClickListener(d -> {
                if(!msg.getText().toString().trim().isEmpty()) {
                    Long ts = (stringToDate(dateSelected[0] + "-" + dateSelected[1] + "-" + dateSelected[2] + " " + timeSelected[0] + "-" + timeSelected[1]).getTime());
                    if (botChannelInfo.getChannel().getIsMember()) {
                        dialog.dismiss();
                        Call<SuccessResponse> call = retrofitCall.scheduleMessage(BOT_TOKEN, id, msg.getText().toString().trim(), String.valueOf(ts / 1000));
                        call.enqueue(new Callback<SuccessResponse>() {
                            @Override
                            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                                if (response.isSuccessful()) {
                                    CustomSnackBar.showSnackBar("Message Scheduled Successfully", MessagesActivity.this);
                                }
                            }

                            @Override
                            public void onFailure(Call<SuccessResponse> call, Throwable t) {

                            }
                        });
                    } else {
                        CustomSnackBar.showSnackBar("Permission Denied! Chatbot is not a member",MessagesActivity.this);
                    }
                }
            });

            dialog.show();
        });

        if (type.equals("channel")) {
            title.setText(name);
//            sendBotBtn.setVisibility(View.VISIBLE);
            timer.setVisibility(View.VISIBLE);
            loadChannelMessages();
            loadChannelInfo();
        } else if(type.equals("dm")) {
            loadUserInfo();
            loadUserMessages();
        } else{
            title.setText(name);
//            sendBotBtn.setVisibility(View.VISIBLE);
            loadChannelMessages();
            loadChannelInfo();
//            sendBotBtn.setVisibility(View.GONE);
        }
    }

    public void onBackPressed() {
        finish();
    }

    private void sendMessage() {
        String msg = typedMessage.getText().toString().trim();
        if (type.equals("channel") && !msg.isEmpty()) {

            if (userChannelInfo.getChannel().getIsMember()) {
                typedMessage.setText("");
                Call<SuccessResponse> call = retrofitCall.sendMessage(USER_TOKEN, id, msg);
                call.enqueue(new Callback<SuccessResponse>() {
                    @Override
                    public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                        if (response.isSuccessful()) {
                            loadChannelMessages();
                        }
                    }

                    @Override
                    public void onFailure(Call<SuccessResponse> call, Throwable t) {

                    }
                });
            } else {
                Log.e(TAG,"something went wrong");
            }

        } else if (!msg.isEmpty()) {
            typedMessage.setText("");
            Call<SuccessResponse> call = retrofitCall.sendMessage(USER_TOKEN, id, msg);
            call.enqueue(new Callback<SuccessResponse>() {
                @Override
                public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                    if (response.isSuccessful()) {
                        loadUserMessages();
                    }
                }

                @Override
                public void onFailure(Call<SuccessResponse> call, Throwable t) {

                }
            });
        }
    }

    private void sendAsBot() {
        String msg = typedMessage.getText().toString().trim();
        if (type.equals("channel") && !msg.isEmpty()) {
            if (botChannelInfo.getChannel().getIsMember()) {
                typedMessage.setText("");
                Call<SuccessResponse> call = retrofitCall.sendMessage(BOT_TOKEN, id, msg);
                call.enqueue(new Callback<SuccessResponse>() {
                    @Override
                    public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                        if (response.isSuccessful()) {
                            loadChannelMessages();
                        }
                    }

                    @Override
                    public void onFailure(Call<SuccessResponse> call, Throwable t) {

                    }
                });
            } else {
                Log.e(TAG,"failed");
            }
        }
    }

    private void loadUserInfo() {
        try {
            Call<UserProfileAPI> call = retrofitCall.userProfile(BOT_TOKEN, user);
            call.enqueue(new Callback<UserProfileAPI>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(Call<UserProfileAPI> call, Response<UserProfileAPI> response) {
                    if (response.isSuccessful()) {
                        title.setText(response.body().getUser().getName());
                    } else {
                        title.setText(id);
                        try {
                            Log.e(TAG, response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserProfileAPI> call, Throwable t) {
                    title.setText(id);
                }
            });
        }
        catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
    }

    private void loadChannelMessages() {
        Call<ChannelMessagesAPI> call = retrofitCall.messagesChannel(USER_TOKEN, id);
        call.enqueue(new Callback<ChannelMessagesAPI>() {
            @Override
            public void onResponse(Call<ChannelMessagesAPI> call, Response<ChannelMessagesAPI> response) {
                if (response.isSuccessful()) {
                    channelMessages = response.body();
                    LinearLayoutManager layoutManager = new LinearLayoutManager(MessagesActivity.this);
                    layoutManager.setReverseLayout(true);
                    messagesRV.setLayoutManager(layoutManager);
                    messagesRV.setAdapter(new ChannelMessagesAdapter(response.body().getMessages(), retrofitCall));
                    messagesRV.scrollToPosition(0);
                    messagesRV.addOnItemTouchListener(new RecyclerItemClickListener(MessagesActivity.this, messagesRV, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            dialog = new Dialog(MessagesActivity.this);
                            View dialogView = LayoutInflater.from(MessagesActivity.this).inflate(R.layout.delete_message_confirm_dialog,null);
                            dialog.setContentView(dialogView);
                            CardView yes = dialogView.findViewById(R.id.cv_delete_confirm);
                            CardView cancel = dialogView.findViewById(R.id.cv_delete_denied);
                            yes.setOnClickListener(v -> {
                                Call<DelMessageAPI> call = retrofitCall.delMessage(USER_TOKEN, id, channelMessages.getMessages().get(position).getTs());
                                call.enqueue(new Callback<DelMessageAPI>() {
                                    @Override
                                    public void onResponse(Call<DelMessageAPI> call, Response<DelMessageAPI> response) {
                                        if (response.isSuccessful()) {
                                            loadUserMessages();
                                        } else {
                                            try {
                                                Log.e(TAG, response.errorBody().string());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<DelMessageAPI> call, Throwable t) {
                                        Log.e(TAG, t.getMessage());
                                    }
                                });
                                dialog.dismiss();
                            });

                            cancel.setOnClickListener(view1 -> {
                                dialog.dismiss();
                            });
                            dialog.show();
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
            public void onFailure(Call<ChannelMessagesAPI> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    private void loadUserMessages() {
        Call<DMMessagesAPI> call = retrofitCall.messagesUser(USER_TOKEN, id);
        call.enqueue(new Callback<DMMessagesAPI>() {
            @Override
            public void onResponse(Call<DMMessagesAPI> call, Response<DMMessagesAPI> response) {
                if (response.isSuccessful()) {
                    userMessages = response.body();
                    LinearLayoutManager layoutManager = new LinearLayoutManager(MessagesActivity.this);
                    layoutManager.setReverseLayout(true);
                    messagesRV.setLayoutManager(layoutManager);
                    messagesRV.setAdapter(new DMMessagesAdapter(response.body().getMessages(), retrofitCall));
                    messagesRV.scrollToPosition(0);
                    messagesRV.addOnItemTouchListener(new RecyclerItemClickListener(MessagesActivity.this, messagesRV, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            dialog = new Dialog(MessagesActivity.this);
                            View dialogView = LayoutInflater.from(MessagesActivity.this).inflate(R.layout.delete_message_confirm_dialog,null);
                            dialog.setContentView(dialogView);
                            CardView yes = dialogView.findViewById(R.id.cv_delete_confirm);
                            CardView cancel = dialogView.findViewById(R.id.cv_delete_denied);
                            yes.setOnClickListener(v -> {
                                Call<DelMessageAPI> call = retrofitCall.delMessage(USER_TOKEN, id, userMessages.getMessages().get(position).getTs());
                                call.enqueue(new Callback<DelMessageAPI>() {
                                    @Override
                                    public void onResponse(Call<DelMessageAPI> call, Response<DelMessageAPI> response) {
                                        if (response.isSuccessful()) {
                                            loadUserMessages();
                                        } else {
                                            try {
                                                Log.e(TAG, response.errorBody().string());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<DelMessageAPI> call, Throwable t) {
                                        Log.e(TAG, t.getMessage());
                                    }
                                });
                                    dialog.dismiss();
                            });

                            cancel.setOnClickListener(v -> {
                                dialog.dismiss();
                            });
                            dialog.show();
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
            public void onFailure(Call<DMMessagesAPI> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    private void loadChannelInfo() {
        Call<ChannelInfoAPI> callU = retrofitCall.channelInfo(USER_TOKEN, id);
        Call<ChannelInfoAPI> callB = retrofitCall.channelInfo(BOT_TOKEN, id);
        callU.enqueue(new Callback<ChannelInfoAPI>() {
            @Override
            public void onResponse(Call<ChannelInfoAPI> call, Response<ChannelInfoAPI> response) {
                if (response.isSuccessful()) {
                    userChannelInfo = response.body();
                    callB.enqueue(new Callback<ChannelInfoAPI>() {
                        @Override
                        public void onResponse(Call<ChannelInfoAPI> call, Response<ChannelInfoAPI> response) {
                            if (response.isSuccessful()) {
                                botChannelInfo = response.body();
                                if(userChannelInfo.getChannel().getIsChannel() && botChannelInfo.getChannel().getIsChannel()){
                                    if (!userChannelInfo.getChannel().getIsMember() || !botChannelInfo.getChannel().getIsMember()) {
                                        dialog = new Dialog(MessagesActivity.this);
                                        View dialogView = LayoutInflater.from(MessagesActivity.this).inflate(R.layout.add_bot_user_dialog,null);
                                        dialog.setContentView(dialogView);
                                        CardView user = dialogView.findViewById(R.id.cv_user);
                                        CardView bot = dialogView.findViewById(R.id.cv_chatbot);
                                        user.setOnClickListener(view -> {
                                            joinChannel(USER_TOKEN,id);
                                            dialog.dismiss();
                                        });
                                        bot.setOnClickListener(view -> {
                                            joinChannel(BOT_TOKEN,id);
                                            dialog.dismiss();
                                        });
                                        dialog.show();
                                    }
                                }
                            } else {
                                try {
                                    Log.e(TAG, response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ChannelInfoAPI> call, Throwable t) {
                            Log.e(TAG, t.getMessage());
                        }
                    });
                } else {
                    try {
                        Log.e(TAG, response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChannelInfoAPI> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    private void joinChannel(String token, String channel) {
        Call<ChannelJoinAPI> call = retrofitCall.joinChannel(token, channel);
        call.enqueue(new Callback<ChannelJoinAPI>() {
            @Override
            public void onResponse(Call<ChannelJoinAPI> call, Response<ChannelJoinAPI> response) {
                if (response.isSuccessful()) {
                    loadChannelMessages();
                    loadChannelInfo();
                } else {
                    try {
                        Log.e(TAG, response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChannelJoinAPI> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }


    private String getDate(Date date) {
        DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        return format.format(date);
    }

    private String getTime(Date date) {
        DateFormat format = new SimpleDateFormat("HH-mm", Locale.ENGLISH);
        return format.format(date);
    }

    private Date stringToDate(String aDate) {
        if (aDate == null) return null;
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-MM-yyyy HH-mm", Locale.ENGLISH);
        return simpledateformat.parse(aDate, pos);

    }
}