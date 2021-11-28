package com.telemedicine.telecare.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.telemedicine.telecare.activity.AdminHomeActivity;
import com.telemedicine.telecare.activity.DoctorHomeActivity;
import com.telemedicine.telecare.activity.PatientHomeActivity;
import com.telemedicine.telecare.helper.NotificationManager;
import com.telemedicine.telecare.model.chat.Group;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.enums.Role;
import com.telemedicine.telecare.util.extensions.NotificationExtensions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MatrikaNotificationService extends FirebaseMessagingService {

    NotificationExtensions          extensions;
    private LocalBroadcastManager   tokenBroadcaster;

    @Override
    public void onCreate() {
        tokenBroadcaster = LocalBroadcastManager.getInstance(this);
        extensions = new NotificationExtensions(this);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.i(Constants.TAG, "New Token: " + token);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        Intent tokenIntent = new Intent(Constants.TOKEN_LISTENER_KEY);
        tokenIntent.putExtra(Constants.TOKEN_INTENT_KEY, token);
        tokenBroadcaster.sendBroadcast(tokenIntent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> getData = remoteMessage.getData();
        if(getData.containsKey(NotificationManager.DATA_KEY)){
            Map<String, String> extraData = new HashMap<>();
            extraData = (Map<String, String>) new Gson().fromJson(getData.get(NotificationManager.DATA_KEY), extraData.getClass());

            if (extraData != null) {
                if (extraData.containsKey("userLoggedIn") && FirebaseAuth.getInstance().getCurrentUser() == null) return;
                else if (extraData.containsKey("allowOnlyInBackground") && extensions.isAppRunInForeground()) return;
                else if (extraData.containsKey("messageSender") && extraData.containsKey("chatGroup")) {
                    Group group = new Gson().fromJson(extraData.get("chatGroup"), Group.class);
                    User sender = new Gson().fromJson(extraData.get("messageSender"), User.class);
                    String receiverId = extraData.get("messageReceiverId");
                    String receiverRoleId = extraData.get("receiverRoleId");
                    if (receiverId == null || receiverRoleId == null || !receiverId.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) return;
                    if (sender != null && group != null) {
                        Intent intent = new Intent(getBaseContext(), receiverRoleId.equals(Role.ADMIN.getId())
                                ? AdminHomeActivity.class
                                : receiverRoleId.equals(Role.PATIENT.getId()) ? PatientHomeActivity.class : DoctorHomeActivity.class
                        );
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constants.CHAT_GROUP_BUNDLE_KEY, group);
                        intent.putExtra(Constants.USER_BUNDLE_KEY, sender);
                        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                        extensions.showNotification(getData.get(NotificationManager.TITLE_KEY), getData.get(NotificationManager.MESSAGE_KEY), contentIntent);
                        return;
                    }
                }
            }
        }

        extensions.showNotification(getData.get(NotificationManager.TITLE_KEY), getData.get(NotificationManager.MESSAGE_KEY), null);
    }
}
