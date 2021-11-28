package com.telemedicine.telecare.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.adapter.DailyFoodsAdapter;
import com.telemedicine.telecare.adapter.SpecialtiesAdapter;
import com.telemedicine.telecare.base.AppBaseActivity;
import com.telemedicine.telecare.fragment.AboutFragment;
import com.telemedicine.telecare.fragment.AddDailyFoodsFragment;
import com.telemedicine.telecare.fragment.AddSugarFragment;
import com.telemedicine.telecare.fragment.AlertDialogFragment;
import com.telemedicine.telecare.fragment.AppointmentScheduleFragment;
import com.telemedicine.telecare.fragment.ChatMessagesFragment;
import com.telemedicine.telecare.fragment.BmiCalculationFragment;
import com.telemedicine.telecare.fragment.LoadingFragment;
import com.telemedicine.telecare.fragment.PatientDoctorsFragment;
import com.telemedicine.telecare.fragment.PatientChatGroupsFragment;
import com.telemedicine.telecare.fragment.PatientProfileFragment;
import com.telemedicine.telecare.fragment.AllUsersFragment;
import com.telemedicine.telecare.fragment.SettingsFragment;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.helper.PermissionManager;
import com.telemedicine.telecare.helper.PreferenceManager;
import com.telemedicine.telecare.model.chat.Group;
import com.telemedicine.telecare.model.food.DailyFood;
import com.telemedicine.telecare.model.other.Status;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.receiver.ReminderReceiver;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.enums.Role;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.telemedicine.telecare.util.extensions.DateExtensions;
import com.telemedicine.telecare.wiget.CircleImageView;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import static android.graphics.Color.TRANSPARENT;

public class PatientHomeActivity extends AppBaseActivity {

    /** Root Layouts **/
    private DrawerLayout            drawer_Layout;
    private LinearLayoutCompat      content_Layout;
    private NavigationView          navigation_Layout;

    /** Toolbar **/
    private AppCompatImageView      toolbar_Menu_Button;
    private FloatingActionButton    addDailyFood_Button;

    /** Content **/
    private AppCompatTextView       doctor_Search_Tv;
    private RecyclerView            rcv_Specialties;
    private SpecialtiesAdapter      specialtiesAdapter;
    private RecyclerView            rcv_DailyFoods;
    private DailyFoodsAdapter       dailyFoods_Adapter;

    /** Drawer **/
    private CircleImageView         nav_UserPhoto;
    private AppCompatTextView       nav_UserName;
    private AppCompatTextView       nav_UserEmail;
    private AppCompatTextView       nav_Profile;
    private AppCompatTextView       nav_Doctors;
    private AppCompatTextView       nav_Chats;
    private AppCompatTextView       nav_Appointments;
    private AppCompatTextView       nav_Bmi;
    private AppCompatTextView       nav_Sugar;
    private AppCompatTextView       nav_SignOut;
    private AppCompatTextView       nav_Settings;
    private AppCompatTextView       nav_About;
    private AppCompatTextView       nav_Share;

    /** Nutritional Needs **/
    private List<DailyFood>         dailyFoods = new ArrayList<>();
    private AppCompatTextView       calorie;
    private AppCompatTextView       sugar;
    private AppCompatTextView       fat;
    private AppCompatTextView       carbohydrate;
    private AppCompatTextView       protein;

    /** Other **/
    private FirebaseHelper          firebaseHelper;
    private LoadingFragment         loading;
    private ListenerRegistration    foodsListenerRegistration;
    private final PreferenceManager pm = new PreferenceManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppExtensions.fullScreenActivity(getWindow(), false);
        setContentView(R.layout.activity_patient_home);
        init();
    }

    private void init(){
        initId();

        setDrawer();

        setNutritionalNeeds();

        setAdapter();

        getDailyFoods();

        setProfileInfo();

        setNotificationData();

        addDailyFood_Button.setOnClickListener(v -> AddDailyFoodsFragment.show());

        doctor_Search_Tv.setOnClickListener(view -> AllUsersFragment.show(Role.DOCTOR));

        specialtiesAdapter.setSpecialties(LocalStorage.specialties);
        this.setOnSpecialtyListener(specialties -> specialtiesAdapter.setSpecialties(LocalStorage.specialties));

        /**
         * Check notification service allowed or not
         **/
        if (!pm.isNotificationServiceForbidded() && !NotificationManagerCompat.from(this).areNotificationsEnabled()) showNotificationServiceEnableDialog();

        /**
         * Check background service enabled or not
         **/
        if (!pm.isBackgroundServiceForbidded() && !pm.isEnabledBackgroundService()) showBackgroundServiceEnableDialog();
    }

    private void initId() {
        drawer_Layout = findViewById(R.id.home_Drawer_Layout);
        content_Layout = findViewById(R.id.home_Content_Layout);
        navigation_Layout = findViewById(R.id.home_Navigation_Layout);

        toolbar_Menu_Button = findViewById(R.id.home_Toolbar_Menu_Button);
        addDailyFood_Button = findViewById(R.id.home_AddDailyFood_Button);

        doctor_Search_Tv = findViewById(R.id.home_Search_Tv);
        rcv_Specialties = findViewById(R.id.home_Specialties_Rcv);
        rcv_DailyFoods = findViewById(R.id.home_DailyFoods_Rcv);

        nav_UserPhoto = findViewById(R.id.home_Nav_UserPhoto_Iv);
        nav_UserName = findViewById(R.id.home_Nav_UserName_Tv);
        nav_UserEmail = findViewById(R.id.home_Nav_UserMail_Tv);
        nav_Profile = findViewById(R.id.home_Nav_Profile);
        nav_Doctors = findViewById(R.id.home_Nav_Doctors);
        nav_Chats = findViewById(R.id.home_Nav_Chats);
        nav_Appointments = findViewById(R.id.home_Nav_Appointments);
        nav_Bmi = findViewById(R.id.home_Nav_Bmi);
        nav_Sugar = findViewById(R.id.home_Nav_Sugar);
        nav_SignOut = findViewById(R.id.home_Nav_SignOut);
        nav_Settings = findViewById(R.id.home_Nav_Settings);
        nav_About = findViewById(R.id.home_Nav_About);
        nav_Share = findViewById(R.id.home_Nav_Share);

        calorie = findViewById(R.id.home_Calories_Tv);
        sugar = findViewById(R.id.home_Sugar_Tv);
        fat = findViewById(R.id.home_Fat_Tv);
        carbohydrate = findViewById(R.id.home_Carbohydrate_Tv);
        protein = findViewById(R.id.home_Protein_Tv);

        firebaseHelper = new FirebaseHelper();
    }

    private void setProfileInfo(){
        AppExtensions.loadPhoto(nav_UserPhoto, LocalStorage.USER.getPhoto(), R.dimen.icon_Size_XXXXX_Large, R.drawable.ic_avatar);

        nav_UserPhoto.setOnClickListener(view -> PatientProfileFragment.show(LocalStorage.USER)
                .setOnProfileUpdateListener(isUpdated -> {
                    if (isUpdated) setProfileInfo();
                })
        );

        nav_UserName.setText(LocalStorage.USER.getName());
        nav_UserEmail.setText(LocalStorage.USER.getEmail());
    }

    private void setAdapter(){
        specialtiesAdapter = new SpecialtiesAdapter();
        rcv_Specialties.setAdapter(specialtiesAdapter);
        specialtiesAdapter.setOnSpecialtySelectListener(specialty -> AllUsersFragment.show(Role.DOCTOR, specialty));

        dailyFoods_Adapter = new DailyFoodsAdapter();
        rcv_DailyFoods.setAdapter(dailyFoods_Adapter);
        dailyFoods_Adapter.setOnFoodListener(new DailyFoodsAdapter.OnFoodListener() {
            @Override
            public void onSelect(DailyFood food) {}

            @Override
            public void onRemove(DailyFood food) {
                AlertDialogFragment.show(R.string.removeFoods, (AppExtensions.getString(R.string.youWantToRemove) + " " + food.getTiming().toLowerCase() + " " + AppExtensions.getString(R.string.foods)), R.string.cancel, R.string.remove)
                        .setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
                            @Override
                            public void onLeftButtonClick() {}

                            @Override
                            public void onRightButtonClick() {
                                loading = LoadingFragment.show();
                                firebaseHelper.setDocumentData(FirebaseFirestore.getInstance()
                                                .collection(FirebaseHelper.DAILY_FOODS_TABLE)
                                                .document(LocalStorage.USER.getId())
                                                .collection(FirebaseHelper.FOODS_COLLECTION)
                                                .document(food.getTiming())
                                                .delete(),
                                        new FirebaseHelper.OnFirebaseUpdateListener() {
                                            @Override
                                            public void onSuccess() { AppExtensions.dismissLoading(loading); }

                                            @Override
                                            public void onFailure() { AppExtensions.dismissLoading(loading); }

                                            @Override
                                            public void onCancelled() { AppExtensions.dismissLoading(loading); }
                                        });
                            }
                        });
            }
        });
    }

    private void getDailyFoods() {
        EventListener<QuerySnapshot> foodsEventListener = (requestSnapshot, error) -> {
            /** Error & Null Data Checking **/
            if (error != null) {
                Log.e(Constants.TAG, "Chat Group Error, Reason: " + error.getCode(), error);
                dailyFoods = new ArrayList<>();
                dailyFoods_Adapter.setDailyFoods(dailyFoods);
                setNutritionalNeeds();
                return;
            }
            else if (requestSnapshot == null || requestSnapshot.isEmpty()) {
                Log.e(Constants.TAG, "No Daily Foods");
                dailyFoods = new ArrayList<>();
                dailyFoods_Adapter.setDailyFoods(dailyFoods);
                setNutritionalNeeds();
                return;
            }

            /** Get Daily Foods **/
            dailyFoods = new ArrayList<>();
            for (QueryDocumentSnapshot snapshot : requestSnapshot) {
                DailyFood dailyFood = snapshot.toObject(DailyFood.class);
                dailyFoods.add(dailyFood);
            }

            dailyFoods_Adapter.setDailyFoods(dailyFoods);
            setNutritionalNeeds();
        };

        foodsListenerRegistration = FirebaseFirestore.getInstance()
                .collection(FirebaseHelper.DAILY_FOODS_TABLE)
                .document(LocalStorage.USER.getId())
                .collection(FirebaseHelper.FOODS_COLLECTION)
                .orderBy(DailyFood.DATE, Query.Direction.ASCENDING)
                .whereGreaterThanOrEqualTo(DailyFood.DATE, new DateExtensions().date())
                .addSnapshotListener(foodsEventListener);
    }

    private void setDrawer() {
        toolbar_Menu_Button.setOnClickListener(v -> drawer_Layout.openDrawer(GravityCompat.START));

        drawer_Layout.setScrimColor(Color.TRANSPARENT);
        drawer_Layout.setDrawerElevation(0);
        drawer_Layout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                float width = navigation_Layout.getWidth() * slideOffset;
                content_Layout.setX(width);
                content_Layout.invalidate();
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {}

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {}

            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        nav_Profile.setOnClickListener(view -> PatientProfileFragment.show(LocalStorage.USER)
                .setOnProfileUpdateListener(isUpdated -> {
                    if (isUpdated) setProfileInfo();
                })
        );
        nav_Doctors.setOnClickListener(view -> PatientDoctorsFragment.show());
        nav_Chats.setOnClickListener(view -> PatientChatGroupsFragment.show());
        nav_Appointments.setOnClickListener(view -> AppointmentScheduleFragment.show());
        nav_Bmi.setOnClickListener(view -> BmiCalculationFragment.show().setOnChangeListener(this::setNutritionalNeeds));
        nav_Sugar.setOnClickListener(view -> AddSugarFragment.show().setOnChangeListener(this::setNutritionalNeeds));

        nav_SignOut.setOnClickListener(v -> {
            loading = LoadingFragment.show();
            HashMap<String, Object> activeStatus = new HashMap<>();
            activeStatus.put(Status.STATUS, false);
            activeStatus.put(Status.DATE, new Date());

            firebaseHelper.setDocumentData(FirebaseFirestore.getInstance()
                    .collection(FirebaseHelper.USERS_TABLE)
                    .document(LocalStorage.USER.getId())
                    .update(User.ACTIVE, activeStatus), new FirebaseHelper.OnFirebaseUpdateListener() {
                @Override
                public void onSuccess() {
                    AppExtensions.dismissLoading(loading);

                    FirebaseAuth.getInstance().signOut();
                    LocalStorage.setUserInfo(null, true);

                    new ReminderReceiver().stopReminder(PatientHomeActivity.this);

                    Intent intent = new Intent(PatientHomeActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                @Override
                public void onFailure() { AppExtensions.dismissLoading(loading); }

                @Override
                public void onCancelled() { AppExtensions.dismissLoading(loading); }
            });
        });

        nav_Settings.setOnClickListener(view -> SettingsFragment.show()
                .setOnSettingsUpdateListener(isUpdated -> {
                    if (isUpdated) setProfileInfo();
                })
        );
        nav_About.setOnClickListener(view -> AboutFragment.show());
        nav_Share.setOnClickListener(view -> AppExtensions.shareApk());
    }

    private void setNutritionalNeeds(){
        boolean hasCalorie = LocalStorage.USER.getBmi() != null && LocalStorage.USER.getBmi().getCalorie() != null;
        double dailyMinCalorie = hasCalorie ? LocalStorage.USER.getBmi().getCalorie().getMin() : 0.0;
        double dailyMaxCalorie = hasCalorie ? LocalStorage.USER.getBmi().getCalorie().getMax() : 0.0;

        boolean hasSugarLevel = LocalStorage.USER.getSugarLevel() != null && LocalStorage.USER.getSugarLevel().getValue() != null;
        double dailySugarLevel = hasSugarLevel ? LocalStorage.USER.getSugarLevel().getValue() : 0.0;

        boolean hasFat = LocalStorage.USER.getBmi() != null && LocalStorage.USER.getBmi().getFat() != null;
        double dailyMinFat = hasFat ? LocalStorage.USER.getBmi().getFat().getMin() : 0.0;
        double dailyMaxFat = hasFat ? LocalStorage.USER.getBmi().getFat().getMax() : 0.0;

        boolean hasCarboHydrate = LocalStorage.USER.getBmi() != null && LocalStorage.USER.getBmi().getCarbohydrate() != null;
        double dailyMinCarboHydrate = hasCarboHydrate ? LocalStorage.USER.getBmi().getCarbohydrate().getMin() : 0.0;
        double dailyMaxCarboHydrate = hasCarboHydrate ? LocalStorage.USER.getBmi().getCarbohydrate().getMax() : 0.0;

        boolean hasProtein = LocalStorage.USER.getBmi() != null && LocalStorage.USER.getBmi().getProtein() != null;
        double dailyMinProtein = hasProtein ? LocalStorage.USER.getBmi().getProtein().getMin() : 0.0;
        double dailyMaxProtein = hasProtein ? LocalStorage.USER.getBmi().getProtein().getMax() : 0.0;

        double foodsCalorie = 0.0, foodsSugar = 0.0, foodsFat = 0.0, foodsCarbohydrate = 0.0, foodsProtein = 0.0;

        findViewById(R.id.home_Divider_View).setVisibility(dailyFoods.isEmpty() ? View.GONE : View.VISIBLE);
        findViewById(R.id.home_TotalData_View).setVisibility(dailyFoods.isEmpty() ? View.GONE : View.VISIBLE);

        for(DailyFood dailyFood : dailyFoods){
            foodsCalorie += (dailyFood.getTotalCalories() == null ? 0.0 : dailyFood.getTotalCalories());
            foodsSugar += (dailyFood.getTotalSugars() == null ? 0.0 : dailyFood.getTotalSugars());
            foodsFat += (dailyFood.getTotalFat() == null ? 0.0 : dailyFood.getTotalFat());
            foodsCarbohydrate += (dailyFood.getTotalCarboHydrate() == null ? 0.0 : dailyFood.getTotalCarboHydrate());
            foodsProtein += (dailyFood.getTotalProtein() == null ? 0.0 : dailyFood.getTotalProtein());
        }

        this.calorie.setText(formatValue(foodsCalorie, dailyMaxCalorie, "", (foodsCalorie >= dailyMinCalorie && foodsCalorie <= dailyMaxCalorie ? "#3FE1DF" : "#E8899E")));
        this.sugar.setText(String.format(Locale.getDefault(),
                "%s%s / %s%s",
                AppExtensions.decimalFormat(foodsSugar, "#.##", false, "0"),
                AppExtensions.getString(R.string.g),
                AppExtensions.decimalFormat(dailySugarLevel, "#.##", false, "0"),
                AppExtensions.getString(R.string.sugarUnit)
        ));
        this.fat.setText(formatValue(foodsFat, dailyMaxFat, AppExtensions.getString(R.string.g), (foodsFat >= dailyMinFat && foodsFat <= dailyMaxFat ? "#3FE1DF" : "#E8899E")));
        this.carbohydrate.setText(formatValue(foodsCarbohydrate, dailyMaxCarboHydrate, AppExtensions.getString(R.string.g), (foodsCarbohydrate >= dailyMinCarboHydrate && foodsCarbohydrate <= dailyMaxCarboHydrate ? "#3FE1DF" : "#E8899E")));
        this.protein.setText(formatValue(foodsProtein, dailyMaxProtein, AppExtensions.getString(R.string.g), (foodsProtein >= dailyMinProtein && foodsProtein <= dailyMaxProtein ? "#3FE1DF" : "#E8899E")));
    }

    private Spanned formatValue(double foodsValue, double dailyValue, String unit, String colorCode){
        String fontColor = foodsValue == 0 ? "#6B779A" : colorCode;
        String foodsValueStr = String.format(Locale.getDefault(),
                "%s%s",
                AppExtensions.decimalFormat(foodsValue, "#.##", false, "0"), unit);

        String dailyValueStr = String.format(Locale.getDefault(),
                "%s%s",
                AppExtensions.decimalFormat(dailyValue, "#.##", false, "0"), unit);

        return AppExtensions.getHtmlString("<font color='" + fontColor + "'>" + foodsValueStr + "</font>" + " / " + dailyValueStr);
    }

    private void setNotificationData() {
        if(getIntent().hasExtra(Constants.USER_BUNDLE_KEY)) {
            User user = (User) getIntent().getSerializableExtra(Constants.USER_BUNDLE_KEY);
            getIntent().removeExtra(Constants.USER_BUNDLE_KEY);
            Group group = (Group) getIntent().getSerializableExtra(Constants.CHAT_GROUP_BUNDLE_KEY);
            getIntent().removeExtra(Constants.CHAT_GROUP_BUNDLE_KEY);

            ChatMessagesFragment.show(user, group, null);
        }
    }

    public void showNotificationServiceEnableDialog(){
        View view = LayoutInflater.from(PatientHomeActivity.this).inflate(R.layout.layout_notification_service_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PatientHomeActivity.this);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setCancelable(false);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(alertDialog.getWindow()).getDecorView().setBackgroundColor(TRANSPARENT);
        alertDialog.setCancelable(false);
        alertDialog.show();

        final AppCompatCheckBox dontAsk_Checkbox = view.findViewById(R.id.notification_DontAsk_CheckBox);
        final AppCompatButton allow_Button = view.findViewById(R.id.notification_Allow_Button);
        final AppCompatButton forbid_Button = view.findViewById(R.id.notification_Forbid_Button);

        dontAsk_Checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> pm.setForbidNotificationService(isChecked));

        allow_Button.setOnClickListener(v -> {
            alertDialog.dismiss();
            new PermissionManager().goToNotificationServiceSetting();
        });

        forbid_Button.setOnClickListener(v -> alertDialog.dismiss());
    }

    public void showBackgroundServiceEnableDialog(){
        View view = LayoutInflater.from(PatientHomeActivity.this).inflate(R.layout.layout_background_service_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PatientHomeActivity.this);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setCancelable(false);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(alertDialog.getWindow()).getDecorView().setBackgroundColor(TRANSPARENT);
        alertDialog.setCancelable(false);
        alertDialog.show();

        final AppCompatCheckBox dontAsk_Checkbox = view.findViewById(R.id.backgroundService_DontAsk_CheckBox);
        final AppCompatButton allow_Button = view.findViewById(R.id.backgroundService_Allow_Button);
        final AppCompatButton forbid_Button = view.findViewById(R.id.backgroundService_Forbid_Button);

        dontAsk_Checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> pm.setForbidBackgroundService(isChecked));

        allow_Button.setOnClickListener(v -> {
            alertDialog.dismiss();
            pm.enableBackgroundService(true);
            new PermissionManager().goToBackgroundServiceSetting();
        });

        forbid_Button.setOnClickListener(v -> alertDialog.dismiss());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(foodsListenerRegistration != null) foodsListenerRegistration.remove();
    }
}