package com.telemedicine.telecare.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.base.AppBaseActivity;
import com.telemedicine.telecare.fragment.AddFoodFragment;
import com.telemedicine.telecare.fragment.ChatMessagesFragment;
import com.telemedicine.telecare.fragment.LoadingFragment;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.helper.PermissionManager;
import com.telemedicine.telecare.helper.PreferenceManager;
import com.telemedicine.telecare.model.chat.Group;
import com.telemedicine.telecare.model.other.Status;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.CustomPopup;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.extensions.AppExtensions;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import static android.graphics.Color.TRANSPARENT;

@SuppressLint("NonConstantResourceId")
public class AdminHomeActivity extends AppBaseActivity {

    /** Toolbar **/
    public static AppCompatTextView toolbar_title;

    /** Navbar **/
    private BottomNavigationView    navView;
    private NavController           navController;

    /** Other **/
    private LoadingFragment         loading;
    private final PreferenceManager pm = new PreferenceManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppExtensions.fullScreenActivity(getWindow(), false);
        setContentView(R.layout.activity_admin_home);
        init();
    }

    private void init(){
        initId();

        setBottomNavigation();

        setNotificationData();

        /**
         * Check Notification Allowed or not
         **/
        if (!pm.isNotificationServiceForbidded() && !NotificationManagerCompat.from(this).areNotificationsEnabled()) showNotificationDialog();
    }

    private void initId() {
        toolbar_title = findViewById(R.id.home_Toolbar_Title_Tv);
        navView = findViewById(R.id.home_Nav_View);
        navController = Navigation.findNavController(this, R.id.home_Nav_Host_Fragment);
    }

    private void setBottomNavigation() {
        new AppBarConfiguration.Builder(R.id.home_Nav_Chats, R.id.home_Nav_Foods, R.id.home_Nav_Doctors).build();
        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            switch (destination.getId()){
                case R.id.home_Nav_Chats:
                    toolbar_title.setText(AppExtensions.getString(R.string.title_chats));
                    break;
                case R.id.home_Nav_Foods:
                    toolbar_title.setText(AppExtensions.getString(R.string.title_foods));
                    break;

                case R.id.home_Nav_Doctors:
                    toolbar_title.setText(AppExtensions.getString(R.string.title_doctors));
                    break;
            }
        });

        navView.findViewById(R.id.home_Nav_More).setOnClickListener(v -> new CustomPopup(v,
                new String[]{
                        AppExtensions.getString(R.string.addFood),
                        AppExtensions.getString(R.string.signOut),
                }, CustomPopup.Popup.MENU).setOnPopupListener((position, item) -> {

                    switch (position){
                        case 0:
                            AddFoodFragment.show();
                            break;
                        case 1:
                            loading = LoadingFragment.show();
                            HashMap<String, Object> activeStatus = new HashMap<>();
                            activeStatus.put(Status.STATUS, false);
                            activeStatus.put(Status.DATE, new Date());

                            new FirebaseHelper().setDocumentData(FirebaseFirestore.getInstance()
                                    .collection(FirebaseHelper.USERS_TABLE)
                                    .document(LocalStorage.USER.getId())
                                    .update(User.ACTIVE, activeStatus), new FirebaseHelper.OnFirebaseUpdateListener() {
                                @Override
                                public void onSuccess() {
                                    AppExtensions.dismissLoading(loading);

                                    FirebaseAuth.getInstance().signOut();
                                    LocalStorage.setUserInfo(null, true);

                                    Intent intent = new Intent(AdminHomeActivity.this, SignInActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }

                                @Override
                                public void onFailure() { AppExtensions.dismissLoading(loading); }

                                @Override
                                public void onCancelled() { AppExtensions.dismissLoading(loading); }
                            });
                            break;
                    }
        }));
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

    public void showNotificationDialog(){
        View view = LayoutInflater.from(AdminHomeActivity.this).inflate(R.layout.layout_notification_service_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AdminHomeActivity.this);
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
}