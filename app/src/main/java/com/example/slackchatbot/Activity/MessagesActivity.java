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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slackchatbot.Adapter.ChannelMsgAdapter;
import com.example.slackchatbot.Adapter.DmMsgAdapter;
import com.example.slackchatbot.Adapter.ScheduleArrayAdapter;
import com.example.slackchatbot.Class.SlackApiClass;
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

    private final String[] SCHEDULES = {"Once", "weekly", "monthly"};

    OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();
    Retrofit retrofit = new Retrofit.Builder().baseUrl(SlackApiClass.BASE_URL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build();
    private SlackApiClass retrofitCall = retrofit.create(SlackApiClass.class);

    DMMessagesAPI userMessages;
    ChannelMessagesAPI channelMessages;

    RecyclerView messagesRV;
    RelativeLayout back, scheduler;
    TextView messagesTitle;
    Dialog dialog;

    SharedPreferences prefs;

    ImageView sendButton;
    EditText msgTOSend;

    LinearLayout holder;

    String channelOrDm = "", channelOrDm_Id = "", channelOrDm_name = "", channelOrDm_user = ""; //user = "" for channel and user has value for Dms

    ChannelInfoAPI userChannelInfo, botChannelInfo;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        Bundle extras = getIntent().getExtras();
        try {
            channelOrDm = extras.getString("type", "");
            channelOrDm_Id = extras.getString("id", "");
            channelOrDm_name = extras.getString("name", "");
            channelOrDm_user = extras.getString("user", "");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        if (channelOrDm.equals("") || channelOrDm_Id.equals("")) {
            CustomSnackBar.showSnackBar("Something went wrong",MessagesActivity.this);
            super.onBackPressed();
        }

        messagesRV = findViewById(R.id.messages_rv);
        back = findViewById(R.id.messages_back);
        messagesTitle = findViewById(R.id.messages_title);
        msgTOSend = findViewById(R.id.messages_edit_text);
        sendButton = findViewById(R.id.messages_send_btn);
        holder = findViewById(R.id.send_message_holder);
        scheduler = findViewById(R.id.messages_timer);

        sendButton.setOnClickListener(it -> sendMsgAsUser());

        back.setOnClickListener(it -> super.onBackPressed());

        scheduler.setOnClickListener(it -> {
            Dialog dialog = new Dialog(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.schedule_msg_popup, null);
            dialog.setContentView(dialogView);
            Window window = dialog.getWindow();
            assert window != null;
            window.setGravity(Gravity.CENTER);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            int[] repeatSchedule = {0}; // used array instead of int because it is being accessed from inner class
            EditText msgToSchedule = dialogView.findViewById(R.id.et_schedule_message);
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
                    repeatSchedule[0] = i;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // blank for future
                }
            });

            // setting schedule messages via chatbot
            user.setOnClickListener(d -> {
                if(!msgToSchedule.getText().toString().trim().isEmpty()){
                    Long ts = (stringToDate(dateSelected[0] + "-" + dateSelected[1] + "-" + dateSelected[2] + " " + timeSelected[0] + "-" + timeSelected[1]).getTime());
                    if(userChannelInfo.getChannel().getIsMember()){
                        dialog.dismiss();
                        int n = 0;
                        int k = 0;
                        if(repeatSchedule[0]==0){ n = 1; k = 0; }

                        else if(repeatSchedule[0]==1){ n = 4; k = 7; }

                        else if(repeatSchedule[0]==2){ n = 6; k = 30; }

                        for(int i=0; i<n; i++){
                            try {
                                Call<SuccessResponse> call = retrofitCall.scheduleMessage(USER_TOKEN, channelOrDm_Id, msgToSchedule.getText().toString().trim(), String.valueOf((ts/1000)+(i*86400*k)));
                                call.enqueue(new Callback<SuccessResponse>() {
                                    @Override
                                    public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                                        if (response.isSuccessful()) {
                                            CustomSnackBar.showSnackBar("Message Scheduled Successfully", MessagesActivity.this);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<SuccessResponse> call, Throwable t) {
                                        Log.e(TAG, "user scheduling failed " + t.getMessage());
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "user scheduling exception " + e.getMessage());
                            }
                        }
                    }else{
                        CustomSnackBar.showSnackBar("Permission Denied! Not a member",MessagesActivity.this);
                    }
                }
            });

            // setting schedule messages via chatbot
            chatbot.setOnClickListener(d -> {
                if(!msgToSchedule.getText().toString().trim().isEmpty()) {
                    Long ts = (stringToDate(dateSelected[0] + "-" + dateSelected[1] + "-" + dateSelected[2] + " " + timeSelected[0] + "-" + timeSelected[1]).getTime());
                    if (botChannelInfo.getChannel().getIsMember()) {
                        dialog.dismiss();
                        int n = 0;
                        int k = 0;
                        if(repeatSchedule[0]==0){ n = 1; k = 0; }

                        else if(repeatSchedule[0]==1){ n = 4; k = 7; }

                        else if(repeatSchedule[0]==2){ n = 6; k = 30; }

                        for(int i=0; i<n; i++){
                            try {
                                Call<SuccessResponse> call = retrofitCall.scheduleMessage(BOT_TOKEN, channelOrDm_Id, msgToSchedule.getText().toString().trim(), String.valueOf((ts/1000)+(i*86400*k)));
                                call.enqueue(new Callback<SuccessResponse>() {
                                    @Override
                                    public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                                        if (response.isSuccessful()) {
                                            CustomSnackBar.showSnackBar("Message Scheduled Successfully", MessagesActivity.this);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<SuccessResponse> call, Throwable t) {
                                        Log.e(TAG, "chatbot scheduling failed " + t.getMessage());
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "chatbot scheduling exception " + e.getMessage());
                            }
                        }
                    } else {
                        CustomSnackBar.showSnackBar("Permission Denied! Chatbot is not a member",MessagesActivity.this);
                    }
                }
            });

            dialog.show();
        });

        if (channelOrDm.equals("channel")) {
            messagesTitle.setText(channelOrDm_name);
            scheduler.setVisibility(View.VISIBLE);
            getChannelMsg();
            checkMember();
        } else if(channelOrDm.equals("dm")) {
            loadDmTitle();
            getDmMsg();
        } else{
            messagesTitle.setText(channelOrDm_name);
            getChannelMsg();
            checkMember();
        }
    }

    public void onBackPressed() {
        finish();
    }

    //to send msg as an user
    private void sendMsgAsUser() {
        String msg = msgTOSend.getText().toString().trim();
        if (channelOrDm.equals("channel") && !msg.isEmpty()) {

            if (userChannelInfo.getChannel().getIsMember()) {
                msgTOSend.setText("");
                Call<SuccessResponse> call = retrofitCall.sendMessage(USER_TOKEN, channelOrDm_Id, msg);
                call.enqueue(new Callback<SuccessResponse>() {
                    @Override
                    public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                        if (response.isSuccessful()) {
                            getChannelMsg();
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
            msgTOSend.setText("");
            Call<SuccessResponse> call = retrofitCall.sendMessage(USER_TOKEN, channelOrDm_Id, msg);
            call.enqueue(new Callback<SuccessResponse>() {
                @Override
                public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                    if (response.isSuccessful()) {
                        getDmMsg();
                    }
                }

                @Override
                public void onFailure(Call<SuccessResponse> call, Throwable t) {

                }
            });
        }
    }

    //load user info to get dm chat title with user name
    private void loadDmTitle() {
        try {
            Call<UserProfileAPI> call = retrofitCall.userProfile(BOT_TOKEN, channelOrDm_user);
            call.enqueue(new Callback<UserProfileAPI>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(Call<UserProfileAPI> call, Response<UserProfileAPI> response) {
                    if (response.isSuccessful()) {
                        messagesTitle.setText(response.body().getUser().getName());
                    } else {
                        messagesTitle.setText(channelOrDm_Id);
                        try {
                            Log.e(TAG, response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserProfileAPI> call, Throwable t) {
                    messagesTitle.setText(channelOrDm_Id);
                }
            });
        }
        catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
    }

    // load all the Channel Messages
    private void getChannelMsg() {
        Call<ChannelMessagesAPI> call = retrofitCall.getChannelMsg(USER_TOKEN, channelOrDm_Id);
        call.enqueue(new Callback<ChannelMessagesAPI>() {
            @Override
            public void onResponse(Call<ChannelMessagesAPI> call, Response<ChannelMessagesAPI> response) {
                if (response.isSuccessful()) {
                    channelMessages = response.body();
                    LinearLayoutManager layoutManager = new LinearLayoutManager(MessagesActivity.this);
                    layoutManager.setReverseLayout(true);
                    messagesRV.setLayoutManager(layoutManager);
                    messagesRV.setAdapter(new ChannelMsgAdapter(response.body().getMessages(), retrofitCall));
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
                                Call<DelMessageAPI> call = retrofitCall.delMessage(USER_TOKEN, channelOrDm_Id, channelMessages.getMessages().get(position).getTs());
                                call.enqueue(new Callback<DelMessageAPI>() {
                                    @Override
                                    public void onResponse(Call<DelMessageAPI> call, Response<DelMessageAPI> response) {
                                        if (response.isSuccessful()) {
                                            getDmMsg();
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

    // load all the Direct Messages
    private void getDmMsg() {
        Call<DMMessagesAPI> call = retrofitCall.getDmMsg(USER_TOKEN, channelOrDm_Id);
        call.enqueue(new Callback<DMMessagesAPI>() {
            @Override
            public void onResponse(Call<DMMessagesAPI> call, Response<DMMessagesAPI> response) {
                if (response.isSuccessful()) {
                    userMessages = response.body();
                    LinearLayoutManager layoutManager = new LinearLayoutManager(MessagesActivity.this);
                    layoutManager.setReverseLayout(true);
                    messagesRV.setLayoutManager(layoutManager);
                    messagesRV.setAdapter(new DmMsgAdapter(response.body().getMessages(), retrofitCall));
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
                                Call<DelMessageAPI> call = retrofitCall.delMessage(USER_TOKEN, channelOrDm_Id, userMessages.getMessages().get(position).getTs());
                                call.enqueue(new Callback<DelMessageAPI>() {
                                    @Override
                                    public void onResponse(Call<DelMessageAPI> call, Response<DelMessageAPI> response) {
                                        if (response.isSuccessful()) {
                                            getDmMsg();
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

    // to check if user and chatbot both are member of the specific public channel
    private void checkMember() {
        Call<ChannelInfoAPI> checkUserMember = retrofitCall.checkMember(USER_TOKEN, channelOrDm_Id);
        Call<ChannelInfoAPI> checkBotMember = retrofitCall.checkMember(BOT_TOKEN, channelOrDm_Id);
        checkUserMember.enqueue(new Callback<ChannelInfoAPI>() {
            @Override
            public void onResponse(Call<ChannelInfoAPI> call, Response<ChannelInfoAPI> response) {
                if (response.isSuccessful()) {
                    userChannelInfo = response.body();
                    checkBotMember.enqueue(new Callback<ChannelInfoAPI>() {
                        @Override
                        public void onResponse(Call<ChannelInfoAPI> call, Response<ChannelInfoAPI> response) {
                            if (response.isSuccessful()) {
                                botChannelInfo = response.body();
                                if(userChannelInfo.getChannel().getIsChannel() && botChannelInfo.getChannel().getIsChannel()){
                                    if (!userChannelInfo.getChannel().getIsMember() || !botChannelInfo.getChannel().getIsMember()) { //if either bot or user are not part of this channel then ask for join
                                        dialog = new Dialog(MessagesActivity.this);
                                        View dialogView = LayoutInflater.from(MessagesActivity.this).inflate(R.layout.add_member_popup,null);
                                        dialog.setContentView(dialogView);
                                        CardView user = dialogView.findViewById(R.id.cv_user);
                                        CardView bot = dialogView.findViewById(R.id.cv_chatbot);
                                        user.setOnClickListener(view -> {
                                            addChannelMember(USER_TOKEN, channelOrDm_Id);
                                            dialog.dismiss();
                                        });
                                        bot.setOnClickListener(view -> {
                                            addChannelMember(BOT_TOKEN, channelOrDm_Id);
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

    //adding user or chatbot to the channel
    private void addChannelMember(String token, String channel) {
        Call<ChannelJoinAPI> call = retrofitCall.addChannelMember(token, channel);
        call.enqueue(new Callback<ChannelJoinAPI>() {
            @Override
            public void onResponse(Call<ChannelJoinAPI> call, Response<ChannelJoinAPI> response) {
                if (response.isSuccessful()) {
                    getChannelMsg();
                    checkMember();
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