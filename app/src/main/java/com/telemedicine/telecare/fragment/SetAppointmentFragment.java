package com.telemedicine.telecare.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.adapter.DaysAdapter;
import com.telemedicine.telecare.adapter.TimesAdapter;
import com.telemedicine.telecare.adapter.PopupMonthsAdapter;
import com.telemedicine.telecare.base.AppBaseActivity;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.model.appointment.Appointment;
import com.telemedicine.telecare.model.appointment.AppointmentTiming;
import com.telemedicine.telecare.model.appointment.Timing;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.CustomPopup;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.enums.AppointmentStatus;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.telemedicine.telecare.util.extensions.DateExtensions;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class SetAppointmentFragment extends DialogFragment implements AppBaseActivity.OnAppointmentListener {

    private static final String TAG = SetAppointmentFragment.class.getSimpleName();
    private Activity            activity;
    private Context             context;
    private AppCompatTextView   month_Input;
    private RecyclerView        days_Rcv;
    private DaysAdapter         days_Adapter;
    private RecyclerView        times_Rcv;
    private TimesAdapter        times_Adapter;
    private AppCompatCheckBox   isPatient_Checkbox;
    private LinearLayoutCompat  name_Layout;
    private AppCompatEditText   name_Input;
    private LinearLayoutCompat  age_Layout;
    private AppCompatEditText   age_Input;
    private LinearLayoutCompat  gender_Layout;
    private AppCompatTextView   gender_Male;
    private AppCompatTextView   gender_Female;
    private AppCompatCheckBox   isReport_Checkbox;
    private AppCompatCheckBox   isConsultation_Checkbox;
    private AppCompatEditText   notes_Input;
    private AppCompatButton     set_Button;
    private AppCompatTextView   title;
    private AppCompatImageView  back_Button;
    private AppCompatImageView  more_Button;
    private User                doctor;
    private Appointment         appointment;
    private AppointmentTiming   chamberInfo;
    private Object[]            selected_Month;
    private Object[]            selected_Day;
    private Object[]            selected_Time;
    private String              selected_Gender;

    private FirebaseHelper      firebaseHelper;
    private LoadingFragment     loading;

    public static SetAppointmentFragment show(User doctor, AppointmentTiming chamberInfo){
        SetAppointmentFragment fragment = new SetAppointmentFragment();
        if(doctor != null){
            Bundle args = new Bundle();
            args.putSerializable(Constants.DOCTOR_BUNDLE_KEY, doctor);
            args.putSerializable(Constants.APPOINTMENT_TIMING_BUNDLE_KEY, chamberInfo);
            fragment.setArguments(args);
        }
        fragment.show(((AppCompatActivity) AppController.getActivity()).getSupportFragmentManager(), TAG);
        return fragment;
    }

    public static SetAppointmentFragment show(Appointment appointment){
        SetAppointmentFragment fragment = new SetAppointmentFragment();
        if(appointment != null){
            Bundle args = new Bundle();
            args.putSerializable(Constants.APPOINTMENT_BUNDLE_KEY, appointment);
            fragment.setArguments(args);
        }
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
        return inflater.inflate(R.layout.fragment_layout_set_apointment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), false);
        super.onViewCreated(view, savedInstanceState);

        initId(view);

        init();
    }

    private void initId(View view) {
        title = view.findViewById(R.id.toolbar_Title_Tv);
        back_Button = view.findViewById(R.id.toolbar_Left_Button);
        more_Button = view.findViewById(R.id.toolbar_Right_Button);

        month_Input = view.findViewById(R.id.appointment_Month_Tv);
        days_Rcv = view.findViewById(R.id.appointment_Days_Rcv);
        times_Rcv = view.findViewById(R.id.appointment_Times_Rcv);
        isPatient_Checkbox = view.findViewById(R.id.appointment_IsPatient_Cb);
        name_Layout = view.findViewById(R.id.appointment_Name_Layout);
        name_Input = view.findViewById(R.id.appointment_PatientName_Input);
        age_Layout = view.findViewById(R.id.appointment_Age_Layout);
        age_Input = view.findViewById(R.id.appointment_PatientAge_Input);
        gender_Layout = view.findViewById(R.id.appointment_Gender_Layout);
        gender_Male = view.findViewById(R.id.appointment_GenderMale_Tv);
        gender_Female = view.findViewById(R.id.appointment_GenderFemale_Tv);
        isConsultation_Checkbox = view.findViewById(R.id.appointment_IsConsultation_Cb);
        isReport_Checkbox = view.findViewById(R.id.appointment_IsReport_Cb);
        notes_Input = view.findViewById(R.id.appointment_Notes_Input);
        set_Button = view.findViewById(R.id.appointment_Set_Button);

        firebaseHelper = new FirebaseHelper();
    }

    private void init() {
        title.setText(AppExtensions.getString(R.string.appointment));

        if(getArguments() != null && getArguments().containsKey(Constants.DOCTOR_BUNDLE_KEY)) {
            doctor = (User) getArguments().getSerializable(Constants.DOCTOR_BUNDLE_KEY);
            getArguments().remove(Constants.DOCTOR_BUNDLE_KEY);
        }

        if(getArguments() != null && getArguments().containsKey(Constants.APPOINTMENT_TIMING_BUNDLE_KEY)) {
            chamberInfo = (AppointmentTiming) getArguments().getSerializable(Constants.APPOINTMENT_TIMING_BUNDLE_KEY);
            getArguments().remove(Constants.APPOINTMENT_TIMING_BUNDLE_KEY);
        }

        if(getArguments() != null && getArguments().containsKey(Constants.APPOINTMENT_BUNDLE_KEY)) {
            appointment = (Appointment) getArguments().getSerializable(Constants.APPOINTMENT_BUNDLE_KEY);
            doctor = new User(appointment.getAppointedTo());
            getArguments().remove(Constants.APPOINTMENT_BUNDLE_KEY);
        }

        back_Button.setOnClickListener(view -> dismiss());

        more_Button.setVisibility(appointment != null ? View.VISIBLE : View.GONE);

        /** Date Time **/
        selected_Month = new DateExtensions().getCurrentMonth();
        month_Input.setText(String.format(Locale.getDefault(), "%s, %s", selected_Month[2], selected_Month[0]));

        setAdapter();
        selected_Day = new DateExtensions().getCurrentDay();
        days_Adapter.setDays(new DateExtensions().getAvailableDays((int) selected_Month[1], (int) selected_Month[0]), (Long) selected_Day[4]);

        month_Input.setOnClickListener(view -> new CustomPopup(view, new PopupMonthsAdapter(context, new DateExtensions().getNextMonths(6)), CustomPopup.Popup.WINDOW)
                .setOnItemClickListener((parent, monthView, pos, id) -> {
                    selected_Day = null;
                    selected_Time = null;
                    selected_Month = (Object[]) parent.getAdapter().getItem(pos);
                    days_Adapter.setDays(new DateExtensions().getAvailableDays((int) selected_Month[1], (int) selected_Month[0]));
                    times_Adapter.setTimes(generateTimes());
                    ((AppCompatTextView) view).setText(String.format(Locale.getDefault(), "%s, %s", selected_Month[2], selected_Month[0]));
                }));

        times_Adapter.setItems(generateTimes(), appointmentSlots());

        /** Is Patient **/
        isPatient_Checkbox.setChecked(appointment == null || appointment.isPatient());
        updateUI("isPatient");
        isPatient_Checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> updateUI("isPatient"));

        /** Gender **/
        selected_Gender = appointment == null ? "Male" : appointment.getPatientGender();
        updateUI("gender");
        gender_Male.setOnClickListener(v -> { selected_Gender = "Male"; updateUI("gender"); });
        gender_Female.setOnClickListener(v -> { selected_Gender = "Female"; updateUI("gender"); });

        /** Patient Details **/
        name_Input.setText(appointment == null || appointment.getPatientName() == null ? "" : appointment.getPatientName());
        age_Input.setText(appointment == null || appointment.getPatientAge() == null ? "" : String.valueOf(appointment.getPatientAge()));

        set_Button.setOnClickListener(view -> {
            String name = isPatient_Checkbox.isChecked()
                    ? LocalStorage.USER.getName()
                    : Objects.requireNonNull(name_Input.getText()).toString().trim();
            String age = isPatient_Checkbox.isChecked()
                    ? String.valueOf(LocalStorage.USER.getAge())
                    : Objects.requireNonNull(age_Input.getText()).toString().trim();
            String gender = isPatient_Checkbox.isChecked()
                    ? LocalStorage.USER.getGender()
                    : selected_Gender;
            String notes = Objects.requireNonNull(notes_Input.getText()).toString().trim();

            if(!isPatient_Checkbox.isChecked()
                    && (!AppExtensions.isInputValid(name_Input, R.string.patientName_Error)
            || !AppExtensions.isInputValid(age_Input, R.string.patientAge_Error)
            || selected_Gender == null)) return;

            if(chamberInfo == null || selected_Month == null || selected_Day == null || selected_Time == null) {
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), AppExtensions.getString(R.string.select_TimeSlot), R.string.okay, CustomSnackBar.Duration.SHORT).show();
                return;
            }

            Appointment appointment = new Appointment();
            appointment.setId(UUID.randomUUID().toString());
            appointment.setAppointedBy(LocalStorage.USER.getId());
            appointment.setAppointedTo(doctor.getId());
            appointment.setPatient(isPatient_Checkbox.isChecked());
            appointment.setConsultation(isConsultation_Checkbox.isChecked());
            appointment.setReport(isReport_Checkbox.isChecked());
            appointment.setPatientName(name);
            appointment.setPatientAge(Integer.parseInt(age));
            appointment.setPatientGender(gender);
            if(!TextUtils.isEmpty(notes)) appointment.setNotes(notes);
            appointment.setStatus(AppointmentStatus.NOT_START);
            appointment.setStartAt(((Date)selected_Time[0]).getTime());
            appointment.setEndAt(((Date)selected_Time[2]).getTime());
            appointment.setChamber(chamberInfo.getChamberAddress());
            appointment.setConsultationTime(((int)selected_Time[4]));

            loading = LoadingFragment.show();
            setAppointment(appointment);
        });
    }

    private void setAppointment(Appointment appointment){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference appointmentReference = db.collection(FirebaseHelper.APPOINTMENTS_TABLE).document(appointment.getId());

        firebaseHelper.setDocumentData(appointmentReference.set(appointment),
                new FirebaseHelper.OnFirebaseUpdateListener() {
                    @Override
                    public void onSuccess() {
                        AppExtensions.dismissLoading(loading);
                        dismiss();
                    }

                    @Override
                    public void onFailure() {
                        AppExtensions.dismissLoading(loading);
                        new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.failureMessage, R.string.okay, CustomSnackBar.Duration.SHORT).show();
                    }

                    @Override
                    public void onCancelled() { AppExtensions.dismissLoading(loading); }
                });
    }

    private void setAdapter() {
        days_Adapter = new DaysAdapter();
        days_Rcv.setAdapter(days_Adapter);
        days_Adapter.setOnDayListener(day -> {
            selected_Day = day;
            selected_Time = null;
            times_Adapter.setTimes(generateTimes());
            days_Adapter.setSelectedDay((Long) selected_Day[4]);
        });

        times_Adapter = new TimesAdapter();
        times_Rcv.setAdapter(times_Adapter);
        times_Adapter.setOnItemListener(item -> {
            selected_Time = item;
            times_Adapter.setSelectedTime(((Date)selected_Time[0]).getTime());
        });
    }

    private void updateUI(String type){
        switch (type){
            case "isPatient":
                boolean isPatient = isPatient_Checkbox.isChecked();
                name_Layout.setVisibility(isPatient ? View.GONE : View.VISIBLE);
                age_Layout.setVisibility(isPatient ? View.GONE : View.VISIBLE);
                gender_Layout.setVisibility(isPatient ? View.GONE : View.VISIBLE);
                break;

            case "gender":
                gender_Male.setBackgroundDrawable(AppExtensions.getDrawable(selected_Gender.equals("Male") ? R.drawable.shape_curve_accent : R.drawable.shape_curve));
                gender_Male.setTextColor(AppExtensions.getColor(selected_Gender.equals("Male") ? R.color.font_Color_Light : R.color.font_Color_Gray));
                gender_Female.setBackgroundDrawable(AppExtensions.getDrawable(selected_Gender.equals("Female") ? R.drawable.shape_curve_accent : R.drawable.shape_curve));
                gender_Female.setTextColor(AppExtensions.getColor(selected_Gender.equals("Female") ? R.color.font_Color_Light : R.color.font_Color_Gray));
                break;
        }
    }

    private List<Object[]> generateTimes(){
        List<Object[]> times = new ArrayList<>();
        if(chamberInfo == null || selected_Month == null || selected_Day == null) return times;

        Date selectedDate = new DateExtensions().date((int) selected_Month[0], (int) selected_Month[1], (int) selected_Day[3]);
        String selectedWeekday = selected_Day[2].toString();
        for (Timing timing : chamberInfo.getTimings()){
            if(!timing.getWeekdays().contains(selectedWeekday)) continue;
            times.addAll(new DateExtensions().getAvailableTimes(selectedDate, timing.getStartTime(), timing.getEndTime(), timing.getConsultationTime()));
        }

        return times;
    }

    List<Date[]> appointmentSlots(){
        List<Date[]> existingSlots = new ArrayList<>();

        for(Appointment appointment : LocalStorage.allAppointments){
            boolean isThisDoctor = doctor.getId().equals(appointment.getAppointedTo());
            if(!isThisDoctor) continue;
            Date[] slot = new Date[2];
            slot[0] = new Date(appointment.getStartAt());
            slot[1] = new Date(appointment.getEndAt());
            existingSlots.add(slot);
        }

        times_Adapter.setExistingSlots(existingSlots);
        return existingSlots;
    }

    @Override
    public void notifyAppointments() {
        appointmentSlots();
    }

    /**
     *  Context Bind
     **/
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
        this.context = context;
    }

    /**
     *  Hide soft keyboard when click outside
     **/
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(activity, getTheme()) {
            @Override
            public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View v = getCurrentFocus();
                    if (v instanceof AppCompatEditText) {
                        Rect outRect = new Rect();
                        v.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            v.clearFocus();
                            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null)
                                imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
                        }
                    }
                }
                return super.dispatchTouchEvent(event);
            }
        };
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
