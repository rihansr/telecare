package com.telemedicine.telecare.helper;

import android.content.Context;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.util.enums.Role;

public class PreferenceManager {

    private final Context   context;

    private final           String USER_MODE_SP_NAME = "userMode";
    private final           String USER_MODE_SP_KEY = "userModeKey";
    private final           String SIGN_IN_SP_NAME = "signInData";
    public static final     String USER_INFO_SP_KEY = "userDetails";
    public static final     String USER_EMAIL_SP_KEY = "userEmailKey";
    public static final     String USER_PASSWORD_SP_KEY = "userPasswordKey";
    public static final     String USER_REMEMBER_SP_KEY = "userRememberKey";

    private final           String FORBID_NOTIFICATION_PERMISSION_SP_NAME = "isNotificationServiceForbid";
    private final           String FORBID_NOTIFICATION_PERMISSION_SP_KEY = "notificationServiceForbidKey";

    private final           String BACKGROUND_SERVICE_SP_NAME = "backgroundService";
    private final           String BACKGROUND_SERVICE_SP_KEY = "backgroundServiceKey";
    private final           String FORBID_BACKGROUND_SERVICE_SP_KEY = "backgroundServiceForbidKey";

    public PreferenceManager(Context context) {
        this.context = context;
    }

    public PreferenceManager() {
        this.context = AppController.getContext();
    }

    public void setUserMode(Role role){
        context.getSharedPreferences(USER_MODE_SP_NAME, Context.MODE_PRIVATE).edit().putInt(USER_MODE_SP_KEY, role.getAction()).apply();
    }

    public Role getUserMode(){
        int mode = context.getSharedPreferences(USER_MODE_SP_NAME, Context.MODE_PRIVATE).getInt(USER_MODE_SP_KEY,1);
        switch (mode){
            default:
            case 0: return Role.ADMIN;
            case 1: return Role.PATIENT;
            case 2: return Role.DOCTOR;
        }
    }

    public void setSignInData(String key, String value){
        context.getSharedPreferences(SIGN_IN_SP_NAME, Context.MODE_PRIVATE).edit().putString(key, value).apply();
    }

    public void setSignInData(String key, boolean state){
        context.getSharedPreferences(SIGN_IN_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(key, state).apply();
    }

    public String getSignInData(String key){
        return context.getSharedPreferences(SIGN_IN_SP_NAME, Context.MODE_PRIVATE).getString(key,null);
    }

    public boolean isUserCheckedRemember(){
        return context.getSharedPreferences(SIGN_IN_SP_NAME, Context.MODE_PRIVATE).getBoolean(USER_REMEMBER_SP_KEY,false);
    }

    public void setForbidNotificationService(boolean isForbidded){
        context.getSharedPreferences(FORBID_NOTIFICATION_PERMISSION_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(FORBID_NOTIFICATION_PERMISSION_SP_KEY, isForbidded).apply();
    }

    public boolean isNotificationServiceForbidded(){
        return context.getSharedPreferences(FORBID_NOTIFICATION_PERMISSION_SP_NAME, Context.MODE_PRIVATE).getBoolean(FORBID_NOTIFICATION_PERMISSION_SP_KEY,false);
    }

    public void enableBackgroundService(boolean isEnabled){
        context.getSharedPreferences(BACKGROUND_SERVICE_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(BACKGROUND_SERVICE_SP_KEY, isEnabled).apply();
    }

    public boolean isEnabledBackgroundService(){
        return context.getSharedPreferences(BACKGROUND_SERVICE_SP_NAME, Context.MODE_PRIVATE).getBoolean(BACKGROUND_SERVICE_SP_KEY,false);
    }

    public void setForbidBackgroundService(boolean isForbidded){
        context.getSharedPreferences(BACKGROUND_SERVICE_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(FORBID_BACKGROUND_SERVICE_SP_KEY, isForbidded).apply();
    }

    public boolean isBackgroundServiceForbidded(){
        return context.getSharedPreferences(BACKGROUND_SERVICE_SP_NAME, Context.MODE_PRIVATE).getBoolean(FORBID_BACKGROUND_SERVICE_SP_KEY,false);
    }
}
