package com.telemedicine.telecare.util;

import com.telemedicine.telecare.util.enums.Role;

public class Constants {

    /**
     * Permission Request
     **/
    public static final int     GPS_PERMISSION_CODE = 1001;
    public static final int     CAMERA_REQUEST_CODE = 1101;
    public static final int     GALLERY_REQUEST_CODE = 1110;

    /**
     * GPS
     **/
    public static final int     DISPLACEMENT = 10;
    public static final int     GPS_UPDATE_INTERVAL = 10000;
    public static final int     GPS_FASTEST_INTERVAL = 5000;

    /** Timer **/
    public static final long    SPLASH_TIME_OUT = 2000;
    public static final long    REMINDER_DELAY = 60000*59; /*59 min*/

    /**
     * Other
     **/
    public static final String  TAG = "Hell";
    public static Role          roleMode = Role.PATIENT;
    public static boolean       IS_INTERNET_CONNECTED = false;
    public static final String  PHONE_PATTERN = "^\\+?(88)?0?1[1356789][0-9]{8}\\b$";
    public static final String  COUNTRY_CODE = "+88";
    public static boolean       IS_SWAPPING = false;

    /**
     * Intent key
     **/
    public static final String SUCCESS_KEY = "Success_Key";
    public static final String PHOTO_BUNDLE_KEY = "photoLinkKey";
    public static final String ROLE_BUNDLE_KEY = "roleBundleKey";
    public static final String SPECIALTY_BUNDLE_KEY = "specialtyBundleKey";
    public static final String CHAT_GROUP_BUNDLE_KEY = "groupBundleKey";
    public static final String MESSAGES_BUNDLE_KEY = "messagesBundleKey";
    public static final String USER_BUNDLE_KEY = "userBundleKey";
    public static final String FOOD_BUNDLE_KEY = "foodBundleKey";
    public static final String FOOD_AMOUNT_BUNDLE_KEY = "foodAmountBundleKey";
    public static final String TIMING_BUNDLE_KEY = "timingKey";
    public static final String APPOINTMENT_TIMING_BUNDLE_KEY = "appointmentTimingKey";
    public static final String APPOINTMENT_BUNDLE_KEY = "appointmentBundleKey";
    public static final String DOCTOR_BUNDLE_KEY = "doctorBundleKey";
    public static final String TOKEN_LISTENER_KEY = "tokenListenerKey";
    public static final String TOKEN_INTENT_KEY = "tokenIntentKey";
}
