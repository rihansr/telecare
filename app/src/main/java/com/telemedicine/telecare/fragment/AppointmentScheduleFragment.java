package com.telemedicine.telecare.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.adapter.AppointmentsAdapter;
import com.telemedicine.telecare.base.AppBaseActivity;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.model.appointment.Appointment;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.enums.AppointmentStatus;
import com.telemedicine.telecare.util.enums.Role;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.telemedicine.telecare.util.extensions.DateExtensions;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppointmentScheduleFragment extends DialogFragment implements AppBaseActivity.OnAppointmentListener {

    private static final String  TAG = AppointmentScheduleFragment.class.getSimpleName();
    private View                 rootView;
    private Activity             activity;

    /**
     * Toolbar
     **/
    private AppCompatImageView   toolbar_Back_Button;

    /**
     * Content
     **/
    private CalendarView         calendar_View;
    private AppCompatTextView    appointment_Title;
    private AppCompatCheckBox    viewAll_Checkbox;
    private RecyclerView         rcv_Appointments;
    private AppointmentsAdapter  appointments_Adapter;

    /**
     * Other
     **/
    private LoadingFragment      loading;
    private FirebaseHelper       firebaseHelper;
    private Date                 selectedDate;

    public static AppointmentScheduleFragment show(){
        AppointmentScheduleFragment fragment = new AppointmentScheduleFragment();
        fragment.show(((AppCompatActivity) AppController.getActivity()).getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_FullDark_FadeAnimation);
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_layout_appointment_schedule, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), false);
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init(){
        initId();

        firebaseHelper = new FirebaseHelper();

        toolbar_Back_Button.setOnClickListener(v -> dismiss());

        calendar_View.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            viewAll_Checkbox.setChecked(false);
            selectedDate = new DateExtensions().date(year, month, dayOfMonth);
            filterAppointments();
            appointment_Title.setText(title());
        });

        viewAll_Checkbox.setChecked(LocalStorage.USER.getRole().equals(Role.PATIENT.getId()));

        appointment_Title.setText(title());

        viewAll_Checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appointment_Title.setText(title());
            filterAppointments();
        });

        setAdapter();

        filterAppointments();

        ((AppBaseActivity) activity).setOnAppointmentListener(this);
    }

    private void initId() {
        toolbar_Back_Button = rootView.findViewById(R.id.appointment_Back_Button);
        calendar_View = rootView.findViewById(R.id.appointment_Calender_Cv);
        appointment_Title = rootView.findViewById(R.id.appointment_Title_Tv);
        viewAll_Checkbox = rootView.findViewById(R.id.appointment_ViewAll_Checkbox);
        rcv_Appointments = rootView.findViewById(R.id.appointment_Appointments_Rcv);
    }

    private void setAdapter() {
        appointments_Adapter = new AppointmentsAdapter();
        rcv_Appointments.setAdapter(appointments_Adapter);
        appointments_Adapter.setOnAppointmentListener(new AppointmentsAdapter.OnAppointmentListener() {
            @Override
            public void onSelect(Appointment appointment, User user) {}

            @Override
            public void onRemove(Appointment appointment) {
                AlertDialogFragment.show(R.string.cancelAppointment, R.string.youWantToCancelAppointment, R.string.no, R.string.cancel)
                        .setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
                            @Override
                            public void onLeftButtonClick() {}

                            @Override
                            public void onRightButtonClick() {
                                cancelAppointment(appointment);
                            }
                        });
            }
        });
    }

    void filterAppointments(){
        List<Appointment> filteredAppointments = new ArrayList<>();
        boolean isADoctor = LocalStorage.USER.getRole().equals(Role.DOCTOR.getId());

        for(Appointment appointment : LocalStorage.myAppointments){
            Date appointmentDate = new Date(appointment.getStartAt());
            if(isADoctor) {
                if(!appointment.getAppointedTo().equals(LocalStorage.USER.getId())) continue;
            }
            else {
                if(!appointment.getAppointedBy().equals(LocalStorage.USER.getId())) continue;
            }

            if(!viewAll_Checkbox.isChecked() && !new DateExtensions().isSameDay(appointmentDate, selectedDate)) continue;

            filteredAppointments.add(appointment);
        }

        appointments_Adapter.setAppointments(filteredAppointments);
    }

    private String title(){
        return AppExtensions.getString(
                !viewAll_Checkbox.isChecked()
                        ? new DateExtensions().isToday(selectedDate == null ? new Date() : selectedDate)
                        ? R.string.todaysAppointment
                        : R.string.appointments
                        : R.string.appointments
        );
    }

    private void cancelAppointment(Appointment appointment){
        loading = LoadingFragment.show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference appointmentReference = db.collection(FirebaseHelper.APPOINTMENTS_TABLE).document(appointment.getId());

        firebaseHelper.setDocumentData(appointmentReference.update(Appointment.STATUS, AppointmentStatus.CANCEL.toString()),
                new FirebaseHelper.OnFirebaseUpdateListener() {
                    @Override
                    public void onSuccess() { AppExtensions.dismissLoading(loading); }

                    @Override
                    public void onFailure() {
                        AppExtensions.dismissLoading(loading);
                        new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.failureMessage, R.string.okay, CustomSnackBar.Duration.SHORT).show();
                    }

                    @Override
                    public void onCancelled() { AppExtensions.dismissLoading(loading); }
                });
    }

    @Override
    public void notifyAppointments() {
        filterAppointments();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
