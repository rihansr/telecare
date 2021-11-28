package com.telemedicine.telecare.util;

import com.hootsuite.nachos.chip.Chip;
import com.telemedicine.telecare.helper.PreferenceManager;
import com.telemedicine.telecare.model.appointment.Appointment;
import com.telemedicine.telecare.model.food.Food;
import com.telemedicine.telecare.model.other.ActivityLevel;
import com.telemedicine.telecare.model.other.Feedback;
import com.telemedicine.telecare.model.other.Request;
import com.telemedicine.telecare.model.specialty.Specialty;
import com.telemedicine.telecare.model.user.User;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class LocalStorage {

    public static User USER = new User();
    public static List<Specialty> specialties = new ArrayList<>();
    public static List<Food> foods = new ArrayList<>();
    public static List<ActivityLevel> activityLevelLevels = new ArrayList<>();
    public static List<String> weekdays = new ArrayList<>();

    public static List<Feedback> allFeedbacks = new ArrayList<>();

    public static List<Appointment> allAppointments = new ArrayList<>();
    public static List<Appointment> myAppointments = new ArrayList<>();

    public static List<Request> allRequests = new ArrayList<>();
    public static List<Request> userRequests = new ArrayList<>();
    public static List<Request> sentRequests = new ArrayList<>();
    public static List<String> sentRequestIds = new ArrayList<>();
    public static List<String> userRequestIds = new ArrayList<>();

    public static void setUserInfo(User userInfo, boolean storeIt){
        PreferenceManager sp = new PreferenceManager();
        String key = PreferenceManager.USER_INFO_SP_KEY;
        if (storeIt) {
            USER = userInfo;
            sp.setSignInData(key, userInfo == null ? null : new Gson().toJson(userInfo));
        }
        else {
            String storedUserInfo = sp.getSignInData(key);
            USER = (storedUserInfo == null) ? new User() : new Gson().fromJson(storedUserInfo, User.class);
        }
    }

    public static void getWeekdays(){
        weekdays = new ArrayList<>();
        weekdays.add("Saturday");
        weekdays.add("Sunday");
        weekdays.add("Monday");
        weekdays.add("Tuesday");
        weekdays.add("Wednesday");
        weekdays.add("Thursday");
        weekdays.add("Friday");
       }

    public static void getActivityLevels(){
        activityLevelLevels = new ArrayList<>();
        activityLevelLevels.add(new ActivityLevel("Sedentary", "little or no exercise", 1.2));
        activityLevelLevels.add(new ActivityLevel("Lightly Active", "light exercise or sports 1-3 days per week", 1.375));
        activityLevelLevels.add(new ActivityLevel("Moderately Active", "moderate exercise or sports 3-5 days per week", 1.55));
        activityLevelLevels.add(new ActivityLevel("Very Active", "hard exercise or sports 6-7 days per week", 1.725));
        activityLevelLevels.add(new ActivityLevel("Super Active", "very hard exercise or sports and a physical job", 1.9));
    }

    public static List<String> getChips(List<Chip> chips){
        if(chips == null || chips.size() == 0) return null;
        List<String> strings = new ArrayList<>();
        for (Chip chip : chips) strings.add(chip.getText().toString());
        return strings;
    }
}
