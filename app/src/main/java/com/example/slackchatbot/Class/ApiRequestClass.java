package com.example.slackchatbot.Class;

import com.example.slackchatbot.Models.ChannelInfoAPI.ChannelInfoAPI;
import com.example.slackchatbot.Models.ChannelJoinAPI.ChannelJoinAPI;
import com.example.slackchatbot.Models.ChannelMessagesAPI.ChannelMessagesAPI;
import com.example.slackchatbot.Models.ChannelsAPI.ChannelsAPI;
import com.example.slackchatbot.Models.DMMessagesAPI.DMMessagesAPI;
import com.example.slackchatbot.Models.DelMessageAPI;
import com.example.slackchatbot.Models.SchedulesMessagesAPI.ScheduledMessagesAPI;
import com.example.slackchatbot.Models.SuccessResponse;
import com.example.slackchatbot.Models.UserProfileAPI.UserProfileAPI;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiRequestClass {

    public static String BASE_URL = "https://slack.com/api/";

    /**user token api*/

    @GET("conversations.list")
    Call<ChannelsAPI> channels(@Query("token") String token, @QueryMap Map<String, String> query);

    @GET("conversations.history")
    Call<ChannelMessagesAPI> messagesChannel(@Query("token") String token, @Query("channel") String id);

    @GET("conversations.history")
    Call<DMMessagesAPI> messagesUser(@Query("token") String token, @Query("channel") String id);

    @POST("conversations.create")
    Call<SuccessResponse> createChannel(@Query("token") String token, @Query("name") String name, @Query("is_private") String is_private);


    /**bot tokens api*/
    @GET("users.info")
    Call<UserProfileAPI> userProfile(@Query("token") String token, @Query("user") String id);







    /**common*/
    @GET("conversations.info")
    Call<ChannelInfoAPI> channelInfo(@Query("token") String token, @Query("channel") String id);

    @POST("conversations.join")
    Call<ChannelJoinAPI> joinChannel(@Query("token") String token, @Query("channel") String id);

    @POST("chat.postMessage")
    Call<SuccessResponse> sendMessage(@Query("token") String token, @Query("channel") String id, @Query("text") String text);

    @POST("chat.delete")
    Call<DelMessageAPI> delMessage(@Query("token") String token, @Query("channel") String id, @Query("ts") String ts);

    @POST("chat.scheduleMessage")
    Call<SuccessResponse> scheduleMessage(@Query("token") String token, @Query("channel") String id, @Query("text") String text, @Query("post_at") String ts);

    @POST("chat.deleteScheduledMessage")
    Call<SuccessResponse> delScheduledMessage(@Query("token") String token, @Query("channel") String channel, @Query("scheduled_message_id") String id);

    @POST("chat.scheduledMessages.list")
    Call<ScheduledMessagesAPI> scheduledMessages(@Query("token") String token);
}
