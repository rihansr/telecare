package com.telemedicine.telecare.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.activity.AdminHomeActivity;
import com.telemedicine.telecare.activity.DoctorHomeActivity;
import com.telemedicine.telecare.activity.SignInActivity;
import com.telemedicine.telecare.activity.SignUpActivity;
import com.telemedicine.telecare.activity.SplashActivity;
import com.telemedicine.telecare.fragment.AlertDialogFragment;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.helper.PreferenceManager;
import com.telemedicine.telecare.model.appointment.Appointment;
import com.telemedicine.telecare.model.food.Food;
import com.telemedicine.telecare.model.other.Feedback;
import com.telemedicine.telecare.model.other.Request;
import com.telemedicine.telecare.model.other.Status;
import com.telemedicine.telecare.model.specialty.Specialty;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.receiver.NetworkStatusChangeReceiver;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.LocalStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class AppBaseActivity extends AppCompatActivity {

    private ListenerRegistration    profileListenerRegistration;
    private OnUserFeedbackListener  mOnUserReviewsListener;

    private ListenerRegistration    requestsListenerRegistration;
    private OnUserRequestListener   mOnUserRequestListener;

    private ListenerRegistration    appointmentsListenerRegistration;
    private OnAppointmentListener   mOnAppointmentListener;

    private ListenerRegistration    feedbacksListenerRegistration;
    private OnUserInfoListener      mOnUserInfoListener;

    private OnSpecialtyListener     mOnSpecialtyListener;
    private OnFoodListener          mOnFoodListener;

    private boolean                 checkIsProfileVerified = false;
    private final FirebaseHelper    firebaseHelper = new FirebaseHelper();
    private final PreferenceManager pm = new PreferenceManager();
    private final NetworkStatusChangeReceiver networkStatusChangeReceiver = new NetworkStatusChangeReceiver();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalStorage.setUserInfo(new User(), false);
        Constants.roleMode = pm.getUserMode();
        switch (Constants.roleMode){
            case PATIENT: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); break;
            case ADMIN:
            case DOCTOR: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); break;
        }

        AppController.setActivity(AppBaseActivity.this);

        checkUserToken();
        getSpecialties();
        getFoods();
        getUserInfo();
        getUserRequests();
        getAppointments();
        getFeedbacks();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppController.setActivity(AppBaseActivity.this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mTokenReceiver, new IntentFilter(Constants.TOKEN_LISTENER_KEY));
        onlinePreference(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkStatusChangeReceiver, new IntentFilter(CONNECTIVITY_ACTION));
    }

    /**
     * Update Active-Status to https://console.firebase.google.com/u/0/project/test-9bed6/firestore/data~2Fusers
     * {@link User model}
     **/
    private void onlinePreference(boolean isOnline){
        if(this instanceof SplashActivity || this instanceof SignInActivity || this instanceof SignUpActivity) return;
        if(LocalStorage.USER == null || LocalStorage.USER.getId() == null) return;
        HashMap<String, Object> activeStatus = new HashMap<>();
        activeStatus.put(Status.STATUS, isOnline);
        activeStatus.put(Status.DATE, new Date());

        firebaseHelper.setDocumentData(FirebaseFirestore.getInstance()
                .collection(FirebaseHelper.USERS_TABLE)
                .document(LocalStorage.USER.getId())
                .update(User.ACTIVE, activeStatus), null);
    }


    /**
     * Get specialties from https://console.firebase.google.com/u/0/project/test-9bed6/firestore/data~2FSpecialties
     * {@link Specialty model}
     **/
    private void getSpecialties() {
        if(this instanceof AdminHomeActivity || this instanceof SplashActivity || this instanceof SignInActivity) return;
        FirebaseFirestore.getInstance().collection(FirebaseHelper.SPECIALTIES_TABLE)
                .orderBy(Specialty.TITLE, Query.Direction.ASCENDING)
                .orderBy(Specialty.SPECIALTY, Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    LocalStorage.specialties = new ArrayList<>();
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Specialty specialty = snapshot.toObject(Specialty.class);
                        LocalStorage.specialties.add(specialty);
                    }

                    if(mOnSpecialtyListener != null) mOnSpecialtyListener.notifySpecialties(LocalStorage.specialties);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Log.e(Constants.TAG, e.toString()+"");
                });
    }


    /**
     * Get foods from https://console.firebase.google.com/u/0/project/test-9bed6/firestore/data~2Ffoods
     * {@link Food model}
     **/
    public void getFoods() {
        if(this instanceof DoctorHomeActivity || this instanceof SplashActivity || this instanceof SignInActivity) return;
        FirebaseFirestore.getInstance().collection(FirebaseHelper.FOODS_TABLE)
                .orderBy(Food.NAME, Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    LocalStorage.foods = new ArrayList<>();
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Food food = snapshot.toObject(Food.class);
                        LocalStorage.foods.add(food);
                    }

                    if(mOnFoodListener != null) mOnFoodListener.notifyFoods(LocalStorage.foods);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Log.e(Constants.TAG, e.toString()+"");
                });
    }


    /**
     * Get user details from https://console.firebase.google.com/u/0/project/test-9bed6/firestore/data~2Fusers
     * {@link User model}
     **/
    private void getUserInfo() {
        if(this instanceof SplashActivity || this instanceof SignInActivity || this instanceof SignUpActivity) return;
        profileListenerRegistration = firebaseHelper.userReference()
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(Constants.TAG, "Profile Load Failed: " + error.getMessage());
                        return;
                    }
                    if (snapshot != null) {
                        User user = snapshot.toObject(User.class);
                        if (user == null) return;
                        LocalStorage.setUserInfo(user, true);
                        if (!user.isProfileVerified() && !checkIsProfileVerified) {
                            AlertDialogFragment.show(R.string.profileNotVerifiedTitle, R.string.profileNotVerifiedMessage, R.string.cancel, R.string.okay);
                            checkIsProfileVerified = true;
                        }
                        if (mOnUserInfoListener != null) mOnUserInfoListener.notifyUserInfo(user);
                    }
                    else signOut();
                });
    }


    /**
     * Check current Device Token
     * Token for sending push notifications
     **/
    private void checkUserToken(){
        if(this instanceof SplashActivity || this instanceof SignInActivity || this instanceof SignUpActivity) return;
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.i(Constants.TAG, "Token Task Failed: ", task.getException());
                return;
            }

            String currentToken = task.getResult();
            updateToken(currentToken);
        });
    }


    /**
     * Update device token (if changed) https://console.firebase.google.com/u/0/project/test-9bed6/firestore/data~2Fusers
     * {@link User model}
     **/
    private void updateToken(String newToken){
        if(this instanceof SplashActivity || this instanceof SignInActivity || this instanceof SignUpActivity) return;
        if(newToken == null) return;
        if (LocalStorage.USER == null) return;
        if (LocalStorage.USER.getToken() == null || !LocalStorage.USER.getToken().equals(newToken)) {
            Map<String, Object> token = new HashMap<>();
            token.put(User.TOKEN, newToken);

            firebaseHelper.setDocumentData(firebaseHelper.userReference().update(token),
                    new FirebaseHelper.OnFirebaseUpdateListener() {
                        @Override
                        public void onSuccess() {
                            LocalStorage.USER.setToken(newToken);
                            LocalStorage.setUserInfo(LocalStorage.USER, true);
                        }

                        @Override
                        public void onFailure() {}

                        @Override
                        public void onCancelled() {}
                    });
        }
    }


    /**
     * Get user requests from https://console.firebase.google.com/u/0/project/test-9bed6/firestore/data~2Frequests
     * {@link Request model}
     **/
    private void getUserRequests(){
        if(this instanceof AdminHomeActivity || this instanceof SplashActivity || this instanceof SignInActivity || this instanceof SignUpActivity) return;
        if(LocalStorage.USER == null || LocalStorage.USER.getId() == null) return;
        EventListener<QuerySnapshot> eventListener = (requestSnapshot, error) -> {

            /** Error Checking **/
            if (error != null) {
                Log.e(Constants.TAG, "Request Error, Reason: " + error.getMessage(), error);
                return;
            }
            else if (requestSnapshot == null || requestSnapshot.isEmpty()) {
                Log.e(Constants.TAG, "No Requests");
                LocalStorage.allRequests.clear();
                LocalStorage.userRequests.clear(); LocalStorage.userRequestIds.clear();
                LocalStorage.sentRequests.clear(); LocalStorage.sentRequestIds.clear();
                if(mOnUserRequestListener != null) mOnUserRequestListener.notifyRequests(LocalStorage.allRequests);
                return;
            }

            /** Get All Requests **/
            LocalStorage.allRequests = new ArrayList<>();
            LocalStorage.userRequests = new ArrayList<>();
            LocalStorage.userRequestIds = new ArrayList<>();
            LocalStorage.sentRequests = new ArrayList<>();
            LocalStorage.sentRequestIds = new ArrayList<>();

            for (QueryDocumentSnapshot snapshot : requestSnapshot) {
                Request request = snapshot.toObject(Request.class);
                if(request.getRequestedTo().equals(LocalStorage.USER.getId()) || request.getRequestedBy().equals(LocalStorage.USER.getId())) LocalStorage.allRequests.add(request);
                if(request.getRequestedTo().equals(LocalStorage.USER.getId())) LocalStorage.userRequests.add(request);
                if(request.getRequestedTo().equals(LocalStorage.USER.getId())) LocalStorage.userRequestIds.add(request.getRequestedBy());
                if(request.getRequestedBy().equals(LocalStorage.USER.getId())) LocalStorage.sentRequests.add(request);
                if(request.getRequestedBy().equals(LocalStorage.USER.getId())) LocalStorage.sentRequestIds.add(request.getRequestedTo());
            }

            if(mOnUserRequestListener != null) mOnUserRequestListener.notifyRequests(LocalStorage.allRequests);
        };

        Query requestQuery = FirebaseFirestore.getInstance().collection(FirebaseHelper.USER_REQUESTS_TABLE)
                .whereEqualTo(Request.REJECTED_STATUS, false);

        requestsListenerRegistration = requestQuery.addSnapshotListener(eventListener);
    }


    /**
     * Get appointments from https://console.firebase.google.com/u/0/project/test-9bed6/firestore/data~2Fappointments
     * {@link Appointment model}
     **/
    private void getAppointments(){
        if(this instanceof AdminHomeActivity || this instanceof SplashActivity || this instanceof SignInActivity || this instanceof SignUpActivity) return;
        if(LocalStorage.USER == null || LocalStorage.USER.getId() == null) return;
        EventListener<QuerySnapshot> eventListener = (appointmentSnapshot, error) -> {

            /** Error Checking **/
            if (error != null) {
                Log.e(Constants.TAG, "Request Error, Reason: " + error.getMessage(), error);
                return;
            }
            else if (appointmentSnapshot == null || appointmentSnapshot.isEmpty()) {
                Log.e(Constants.TAG, "No Appointments");
                LocalStorage.allAppointments.clear();
                LocalStorage.myAppointments.clear();
                if(mOnAppointmentListener != null) mOnAppointmentListener.notifyAppointments();
                return;
            }

            /** Get All Appointments **/
            LocalStorage.allAppointments = new ArrayList<>();
            LocalStorage.myAppointments = new ArrayList<>();

            for (QueryDocumentSnapshot snapshot : appointmentSnapshot) {
                Appointment appointment = snapshot.toObject(Appointment.class);
                LocalStorage.allAppointments.add(appointment);
                if(appointment.getAppointedTo().equals(LocalStorage.USER.getId()) || appointment.getAppointedBy().equals(LocalStorage.USER.getId())) LocalStorage.myAppointments.add(appointment);
            }

            if(mOnAppointmentListener != null) mOnAppointmentListener.notifyAppointments();
        };

        Query requestQuery = FirebaseFirestore.getInstance().collection(FirebaseHelper.APPOINTMENTS_TABLE)
                .whereNotEqualTo(Appointment.STATUS, "CANCEL");

        appointmentsListenerRegistration = requestQuery.addSnapshotListener(eventListener);
    }


    /**
     * Get data from https://console.firebase.google.com/u/0/project/test-9bed6/firestore/data~2Ffeedbacks
     * {@link Feedback} (Table data model)
     **/
    private void getFeedbacks(){
        if(this instanceof AdminHomeActivity || this instanceof SplashActivity || this instanceof SignInActivity || this instanceof SignUpActivity) return;
        if(LocalStorage.USER == null || LocalStorage.USER.getId() == null) return;
        EventListener<QuerySnapshot> followerEventListener = (requestSnapshot, error) -> {

            /** Error Checking **/
            if (error != null) {
                Log.e(Constants.TAG, "Follower Error, Reason: " + error.getMessage(), error);
                return;
            }
            else if (requestSnapshot == null || requestSnapshot.isEmpty()) {
                Log.e(Constants.TAG, "No Feedbacks");
                LocalStorage.allFeedbacks.clear();
                if(mOnUserReviewsListener != null) mOnUserReviewsListener.notifyFeedbacks(LocalStorage.allFeedbacks);
                return;
            }

            /** Get All Feedbacks **/
            LocalStorage.allFeedbacks = new ArrayList<>();
            for (QueryDocumentSnapshot snapshot : requestSnapshot) {
                Feedback feedback = snapshot.toObject(Feedback.class);
                LocalStorage.allFeedbacks.add(feedback);
            }

            if(mOnUserReviewsListener != null) mOnUserReviewsListener.notifyFeedbacks(LocalStorage.allFeedbacks);
        };

        feedbacksListenerRegistration = FirebaseFirestore.getInstance().collection(FirebaseHelper.USER_FEEDBACKS_TABLE)
                .addSnapshotListener(followerEventListener);
    }

    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        LocalStorage.setUserInfo(null, true);

        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private final BroadcastReceiver mTokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String token = intent.getStringExtra(Constants.TOKEN_INTENT_KEY);
            updateToken(token);
            intent.removeExtra(Constants.TOKEN_INTENT_KEY);
        }
    };

    public void setOnUserRequestListener(OnUserRequestListener mOnUserRequestListener) {
        this.mOnUserRequestListener = mOnUserRequestListener;
    }

    public interface OnUserRequestListener {
        void notifyRequests(List<Request> requests);
    }

    public void setOnUserFeedbackListener(OnUserFeedbackListener mOnUserFeedbackListener) {
        this.mOnUserReviewsListener = mOnUserFeedbackListener;
    }

    public interface OnUserFeedbackListener {
        void notifyFeedbacks(List<Feedback> feedbacks);
    }

    public void setOnUserInfoListener(OnUserInfoListener mOnUserInfoListener) {
        this.mOnUserInfoListener = mOnUserInfoListener;
    }

    public interface OnUserInfoListener {
        void notifyUserInfo(User info);
    }

    public void setOnSpecialtyListener(OnSpecialtyListener mOnSpecialtyListener) {
        this.mOnSpecialtyListener = mOnSpecialtyListener;
    }

    public interface OnAppointmentListener {
        void notifyAppointments();
    }

    public void setOnAppointmentListener(OnAppointmentListener mOnAppointmentListener) {
        this.mOnAppointmentListener = mOnAppointmentListener;
    }

    public interface OnSpecialtyListener {
        void notifySpecialties(List<Specialty> specialties);
    }

    public void setOnFoodListener(OnFoodListener mOnFoodListener) {
        this.mOnFoodListener = mOnFoodListener;
    }

    public interface OnFoodListener {
        void notifyFoods(List<Food> foods);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkStatusChangeReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        onlinePreference(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mTokenReceiver);
        if(profileListenerRegistration != null) profileListenerRegistration.remove();
        if(requestsListenerRegistration != null) requestsListenerRegistration.remove();
        if(appointmentsListenerRegistration != null) appointmentsListenerRegistration.remove();
        if(feedbacksListenerRegistration != null) feedbacksListenerRegistration.remove();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * {@link NetworkStatusChangeReceiver} Monitor internet connection
     **/
    public void updateInternetConnectionStatus(boolean isConnected) {
        CustomSnackBar customSnackBar = new CustomSnackBar(R.string.network_Error, R.string.retry, CustomSnackBar.Duration.INDEFINITE);

        if (isConnected) {
            customSnackBar.dismiss();
        }
        else {
            customSnackBar.show();
            customSnackBar.setOnDismissListener(snackBar -> {
                networkStatusChangeReceiver.onReceive(AppController.getActivity(), null);
                snackBar.dismiss();
            });
        }
    }
}
