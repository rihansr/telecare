package com.telemedicine.telecare.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.adapter.UsersAdapter;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.enums.Role;
import com.telemedicine.telecare.util.extensions.AppExtensions;

import java.util.ArrayList;
import java.util.List;

public class NavDoctorsFragment extends Fragment {

    private View                        rootView;
    private RecyclerView                rcv_Doctors;
    private UsersAdapter                doctors_Adapter;

    public static LinearLayoutCompat    empty_Layout;
    private LottieAnimationView         empty_Icon;
    private AppCompatTextView           empty_Title;
    private AppCompatTextView           empty_Subtitle;

    private ListenerRegistration        doctorsListenerRegistration;
    private LoadingFragment             loading;

    public NavDoctorsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_nav_doctors, container, false);
        initId();
        init();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initId() {
        rcv_Doctors = rootView.findViewById(R.id.doctors_Doctors_Rcv);
        empty_Layout = rootView.findViewById(R.id.doctors_Empty_Layout);
        empty_Icon = rootView.findViewById(R.id.empty_Icon_Animation);
        empty_Title = rootView.findViewById(R.id.empty_Title_Tv);
        empty_Subtitle = rootView.findViewById(R.id.empty_Subtitle_Tv);
    }

    private void init() {
        empty_Icon.setAnimation("lottie/blank_users.json");
        empty_Title.setText(AppExtensions.getString(R.string.emptyDoctorRequestsTitle));
        empty_Subtitle.setText(AppExtensions.getString(R.string.emptyDoctorRequestsSubtitle));

        setAdapter();

        showDoctors();
    }

    private void setAdapter(){
        doctors_Adapter = new UsersAdapter(3);
        rcv_Doctors.setAdapter(doctors_Adapter);
    }

    private void showDoctors(){
        loading = LoadingFragment.show();

        EventListener<QuerySnapshot> doctorsEventListener = (requestSnapshot, error) -> {
            AppExtensions.dismissLoading(loading);

            /** Error & Null Data Checking **/
            if (error != null) {
                Log.e(Constants.TAG, "Fetch Doctors Error, Reason: " + error.getCode(), error);
                doctors_Adapter.setUsers(new ArrayList<>());
                NavDoctorsFragment.empty_Layout.setVisibility(View.VISIBLE);
                return;
            }
            else if (requestSnapshot == null || requestSnapshot.isEmpty()) {
                Log.e(Constants.TAG, "No Doctors");
                doctors_Adapter.setUsers(new ArrayList<>());
                NavDoctorsFragment.empty_Layout.setVisibility(View.VISIBLE);
                return;
            }

            /** Get All Doctors **/
            List<User> doctors = new ArrayList<>();
            for (QueryDocumentSnapshot snapshot : requestSnapshot) {
                User user = snapshot.toObject(User.class);
                doctors.add(user);
            }

            doctors_Adapter.setUsers(doctors);
            NavDoctorsFragment.empty_Layout.setVisibility(doctors.size() == 0 ? View.VISIBLE : View.GONE);
        };

        doctorsListenerRegistration = FirebaseFirestore.getInstance()
                .collection(FirebaseHelper.USERS_TABLE)
                .whereEqualTo(User.ROLE, Role.DOCTOR.getId())
                .addSnapshotListener(doctorsEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(doctorsListenerRegistration != null) doctorsListenerRegistration.remove();
    }
}