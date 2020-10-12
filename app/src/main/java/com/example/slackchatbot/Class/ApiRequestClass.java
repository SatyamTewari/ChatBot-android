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

    @GET("conversations.list") //get all channels list
    Call<ChannelsAPI> channels(@Query("token") String token, @QueryMap Map<String, String> query);

    @GET("conversations.history") //to get all the conversations on a particular channel
    Call<ChannelMessagesAPI> messagesChannel(@Query("token") String token, @Query("channel") String id);

    @GET("conversations.history") //to get all the conversations from a particular dm
    Call<DMMessagesAPI> messagesUser(@Query("token") String token, @Query("channel") String id);

    @POST("conversations.create") //create channel
    Call<SuccessResponse> createChannel(@Query("token") String token, @Query("name") String name, @Query("is_private") String is_private);


    /**bot tokens api*/

    @GET("users.info") //for user info of all dm
    Call<UserProfileAPI> userProfile(@Query("token") String token, @Query("user") String id);


    /**common*/

    @GET("conversations.info") //to get channel info like channel name, public/private, etc
    Call<ChannelInfoAPI> channelInfo(@Query("token") String token, @Query("channel") String id);

    @POST("conversations.join") // to join channel
    Call<ChannelJoinAPI> joinChannel(@Query("token") String token, @Query("channel") String id);

    @POST("chat.postMessage") // to send simple messages
    Call<SuccessResponse> sendMessage(@Query("token") String token, @Query("channel") String id, @Query("text") String text);

    @POST("chat.delete") // to delete messages
    Call<DelMessageAPI> delMessage(@Query("token") String token, @Query("channel") String id, @Query("ts") String ts);

    @POST("chat.scheduleMessage") // to create schedule message
    Call<SuccessResponse> scheduleMessage(@Query("token") String token, @Query("channel") String id, @Query("text") String text, @Query("post_at") String ts);

    @POST("chat.deleteScheduledMessage") // to delete schedule message
    Call<SuccessResponse> delScheduledMessage(@Query("token") String token, @Query("channel") String channel, @Query("scheduled_message_id") String id);

    @POST("chat.scheduledMessages.list") // to get the list of scheduled messages of user and bot as per token passed
    Call<ScheduledMessagesAPI> scheduledMessages(@Query("token") String token);
}
