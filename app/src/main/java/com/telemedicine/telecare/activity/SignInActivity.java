package com.telemedicine.telecare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.base.AppBaseActivity;
import com.telemedicine.telecare.fragment.ResetPasswordFragment;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.helper.PermissionManager;
import com.telemedicine.telecare.helper.PreferenceManager;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.receiver.ReminderReceiver;
import com.telemedicine.telecare.util.CircularReveal;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.CustomProgressDialog;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.enums.DrawablePosition;
import com.telemedicine.telecare.util.enums.Role;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.telemedicine.telecare.wiget.ClickableEditText;
import java.util.Objects;

public class SignInActivity extends AppBaseActivity {

    private FrameLayout                 userMode_Button;
    private AppCompatImageView          userMode_Icon;
    private AppCompatImageView          userType_Icon;
    private AppCompatTextView           userType;
    private AppCompatEditText           email_Input;
    private ClickableEditText           password_Input;
    private AppCompatButton             login_Button;
    private AppCompatCheckBox           rememberMe_Checkbox;
    private AppCompatTextView           forgotPassword_Button;
    private AppCompatTextView           register_Button;
    private final PreferenceManager     pm = new PreferenceManager();
    private CustomProgressDialog        progressDialog;
    private CircularReveal              circularReveal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppExtensions.fullScreenActivity(getWindow(), false);
        setContentView(R.layout.activity_sign_in);
        new Handler().postDelayed(() -> new PermissionManager().showPermissionDialogs(), 100);

        if(Constants.IS_SWAPPING){
            circularReveal = new CircularReveal(findViewById(R.id.signIn_Root_Layout));
            circularReveal.onActivityCreate(getIntent());
        }

        initId();

        init();
    }

    private void initId(){
        userMode_Button = findViewById(R.id.signIn_UserMode_Button);
        userMode_Icon = findViewById(R.id.signIn_UserMode_Iv);
        userType_Icon = findViewById(R.id.signIn_UserType_Iv);
        userType = findViewById(R.id.signIn_UserType_Tv);
        email_Input = findViewById(R.id.signIn_Email_Input);
        password_Input = findViewById(R.id.signIn_Password_Input);
        login_Button = findViewById(R.id.signIn_Login_Button);
        rememberMe_Checkbox = findViewById(R.id.signIn_RememberMe_Checkbox);
        forgotPassword_Button = findViewById(R.id.signIn_ForgotPassword_Button);
        register_Button = findViewById(R.id.signIn_Register_Button);
        progressDialog = new CustomProgressDialog();
    }

    private void init(){
        if(pm.isUserCheckedRemember()){
            email_Input.setText(pm.getSignInData(PreferenceManager.USER_EMAIL_SP_KEY));
            password_Input.setText(pm.getSignInData(PreferenceManager.USER_PASSWORD_SP_KEY));
            rememberMe_Checkbox.setChecked(pm.isUserCheckedRemember());
        }

        AppExtensions.doGradientText(userType);
        AppExtensions.doGradientText(register_Button);

        userMode_Button.setOnLongClickListener(v -> {
            userMode_Button.setEnabled(false);
            pm.setUserMode(Constants.roleMode == Role.ADMIN ? Role.PATIENT : Role.ADMIN);

            Intent intent = new Intent(this, SignInActivity.class);
            Constants.IS_SWAPPING = true;
            CircularReveal.Builder builder = new CircularReveal.Builder(this, userMode_Icon, intent, 750);
            builder.setRevealColor(AppExtensions.getColor(R.color.colorReveal));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            CircularReveal.presentActivity(builder);
            return false;
        });

        userMode_Button.setOnClickListener(view -> {
            userMode_Button.setEnabled(false);
            pm.setUserMode(Constants.roleMode == Role.PATIENT ? Role.DOCTOR : Role.PATIENT);
            Intent intent = new Intent(this, SignInActivity.class);
            Constants.IS_SWAPPING = true;
            CircularReveal.Builder builder = new CircularReveal.Builder(this, userMode_Icon, intent, 750);
            builder.setRevealColor(AppExtensions.getColor(R.color.colorReveal));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            CircularReveal.presentActivity(builder);
        });

        switch (Constants.roleMode){
            case PATIENT:
                userMode_Icon.setImageDrawable(AppExtensions.getDrawable(R.drawable.ic_doctor_mini));
                userType_Icon.setImageDrawable(AppExtensions.getDrawable(R.drawable.ic_patient));
                userType.setText(AppExtensions.getString(R.string.patientTitle));
                findViewById(R.id.signIn_Register_Tv).setVisibility(View.VISIBLE);
                findViewById(R.id.signIn_Register_Button).setVisibility(View.VISIBLE);
                break;

            case DOCTOR:
                userMode_Icon.setImageDrawable(AppExtensions.getDrawable(R.drawable.ic_patient_mini));
                userType_Icon.setImageDrawable(AppExtensions.getDrawable(R.drawable.ic_doctor));
                userType.setText(AppExtensions.getString(R.string.doctorTitle));
                findViewById(R.id.signIn_Register_Tv).setVisibility(View.VISIBLE);
                findViewById(R.id.signIn_Register_Button).setVisibility(View.VISIBLE);
                break;

            case ADMIN:
                userMode_Icon.setImageDrawable(AppExtensions.getDrawable(R.drawable.ic_patient_mini));
                userType_Icon.setImageDrawable(AppExtensions.getDrawable(R.drawable.ic_admin));
                userType.setText(AppExtensions.getString(R.string.adminTitle));
                findViewById(R.id.signIn_Register_Tv).setVisibility(View.INVISIBLE);
                findViewById(R.id.signIn_Register_Button).setVisibility(View.INVISIBLE);
                break;
        }

        email_Input.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        if(Constants.IS_INTERNET_CONNECTED) signIn();
                        return true;

                    default: break;
                }
            }
            return false;
        });

        password_Input.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        if(Constants.IS_INTERNET_CONNECTED) signIn();
                        return true;

                    default: break;
                }
            }
            return false;
        });

        password_Input.setOnDrawableClickListener(DrawablePosition.RIGHT, () -> {
            if (password_Input.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                password_Input.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_input_password, 0, R.drawable.ic_password_visibility_off, 0);
                password_Input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            else {
                password_Input.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_input_password, 0, R.drawable.ic_password_visibility_on, 0);
                password_Input.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        login_Button.setOnClickListener(v -> { if(Constants.IS_INTERNET_CONNECTED) signIn(); });

        forgotPassword_Button.setOnClickListener(v ->
                ResetPasswordFragment.show().setOnSuccessListener((email, isSuccessful) ->
                        new Handler().postDelayed(() -> {
                            if (isSuccessful) {
                                new CustomSnackBar("An email has been sent to " + email + ". Follow the directions in the email to reset your password.",
                                        R.string.gotIt, CustomSnackBar.Duration.LONG).show();
                            }
                            else {
                                new CustomSnackBar(R.string.sendingEmailFailed, R.string.okay, CustomSnackBar.Duration.LONG).show();
                            }
                        }, 100)
                ));

        register_Button.setOnClickListener(view -> {
            if(Constants.roleMode == Role.ADMIN) return;
            Constants.IS_SWAPPING = false;
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right);
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void signIn(){
        final String email = Objects.requireNonNull(email_Input.getText()).toString().trim();
        final String password = Objects.requireNonNull(password_Input.getText()).toString().trim();

        if (!AppExtensions.isEmailValid(email_Input, R.string.email_Error) || !AppExtensions.isPasswordValid(password_Input, R.string.password_Error)) return;

        progressDialog.show(R.string.processing, false);

        /**
         * Get user info from https://console.firebase.google.com/u/0/project/test-9bed6/firestore/data~2Fusers
         * {@link User model}
         **/
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseFirestore.getInstance().collection(FirebaseHelper.USERS_TABLE)
                                .document(Objects.requireNonNull(task.getResult().getUser()).getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        progressDialog.dismiss();
                                        User user = documentSnapshot.toObject(User.class);
                                       if(user == null || !user.getRole().equals(Constants.roleMode.getId())) {
                                            new CustomSnackBar(R.string.signInValidityFailed, R.string.okay, CustomSnackBar.Duration.LONG).show();
                                            return;
                                        }
                                        launchNewActivity(user, email, password, rememberMe_Checkbox.isChecked());
                                    }
                                    else {
                                        progressDialog.dismiss();
                                        new CustomSnackBar(R.string.signInValidityFailed, R.string.okay, CustomSnackBar.Duration.LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    new CustomSnackBar(R.string.signInFailed, R.string.retry).show();
                                });
                    } else {
                        progressDialog.dismiss();
                        new CustomSnackBar(R.string.authenticationFailed, R.string.retry, CustomSnackBar.Duration.LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    new CustomSnackBar(R.string.authenticationFailed, R.string.okay, CustomSnackBar.Duration.LONG).show();
                });
    }

    private void launchNewActivity(User user, String email, String password, boolean isChecked) {
        pm.setSignInData(PreferenceManager.USER_INFO_SP_KEY, new Gson().toJson(user));
        pm.setUserMode(user.getRole().equals(Role.ADMIN.getId())
                ? Role.ADMIN
                : user.getRole().equals(Role.PATIENT.getId()) ? Role.PATIENT : Role.DOCTOR
        );
        pm.setSignInData(PreferenceManager.USER_EMAIL_SP_KEY, isChecked ? email : null);
        pm.setSignInData(PreferenceManager.USER_PASSWORD_SP_KEY, isChecked ? password : null);
        pm.setSignInData(PreferenceManager.USER_REMEMBER_SP_KEY, isChecked);

        if(user.getRole().equals(Role.PATIENT.getId())) new ReminderReceiver().startReminder(this);

        Intent intent = new Intent(SignInActivity.this, user.getRole().equals(Role.ADMIN.getId())
                ? AdminHomeActivity.class
                : user.getRole().equals(Role.PATIENT.getId()) ? PatientHomeActivity.class : DoctorHomeActivity.class
        );

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(circularReveal != null) circularReveal.unRevealActivity(this);
    }
}
