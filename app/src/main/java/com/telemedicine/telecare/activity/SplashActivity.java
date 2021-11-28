package com.telemedicine.telecare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.base.AppBaseActivity;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppExtensions.fullScreenActivity(getWindow(), true);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(this::launchNewActivity, Constants.SPLASH_TIME_OUT);
    }

    private void launchNewActivity() {
        Intent intent;
        if(FirebaseAuth.getInstance().getCurrentUser() == null || LocalStorage.USER == null) intent = new Intent(SplashActivity.this, SignInActivity.class);
        else {
            switch (Constants.roleMode){
                case PATIENT:
                    intent = new Intent(SplashActivity.this, PatientHomeActivity.class);
                    break;
                case DOCTOR:
                    intent = new Intent(SplashActivity.this, DoctorHomeActivity.class);
                    break;
                case ADMIN:
                    intent = new Intent(SplashActivity.this, AdminHomeActivity.class);
                    break;
                default:
                    intent = new Intent(SplashActivity.this, SignInActivity.class);
            }
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
