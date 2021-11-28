package com.telemedicine.telecare.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.adapter.TimingAdapter;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.model.appointment.AppointmentTiming;
import com.telemedicine.telecare.model.appointment.Timing;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressLint("SetTextI18n, ClickableViewAccessibility")
public class AddAppointmentFragment extends DialogFragment {

    private static final String         TAG = AddAppointmentFragment.class.getSimpleName();
    private Activity                    activity;
    private AppCompatEditText           chamberAddress_Input;
    private AppCompatEditText           assistantName_Input;
    private AppCompatEditText           assistantPhone_Input;
    private RecyclerView                rcv_Timings;
    private TimingAdapter               timing_Adapter;
    private AppCompatImageButton        addTiming_Button;
    private AppCompatButton             add_Button;
    private AppCompatImageView          back_Button;
    private List<Timing>                timings;

    private LoadingFragment             loading;
    private FirebaseHelper              firebaseHelper;
    private OnAppointmentTimingListener mOnAppointmentTimingListener;

    public static AddAppointmentFragment show(){
        AddAppointmentFragment fragment = new AddAppointmentFragment();
        fragment.show(((AppCompatActivity) AppController.getActivity()).getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        AppExtensions.halfScreenDialog(getDialog());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_add_appointment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initId(view);

        init();
    }

    private void initId(View view) {
        chamberAddress_Input = view.findViewById(R.id.appointment_ChamberAddress_Input);
        assistantName_Input = view.findViewById(R.id.appointment_AssistantName_Input);
        assistantPhone_Input = view.findViewById(R.id.appointment_AssistantPhone_Input);
        rcv_Timings = view.findViewById(R.id.appointment_Timings_Rcv);
        addTiming_Button = view.findViewById(R.id.appointment_AddTiming_Button);
        add_Button = view.findViewById(R.id.appointment_Add_Button);
        back_Button = view.findViewById(R.id.appointment_Back_Button);
    }

    private void init() {
        firebaseHelper = new FirebaseHelper();

        timings = new ArrayList<>();

        setAdapter();

        back_Button.setOnClickListener(view -> dismiss());

        assistantPhone_Input.setOnFocusChangeListener((v, hasFocus) ->
                assistantPhone_Input.setHint(AppExtensions.getString(hasFocus ? R.string.dummyContactNo : R.string.phone_Hint)));

        assistantPhone_Input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(AppExtensions.isNumberValidate(s.toString())){
                    AppExtensions.hideKeyboard(activity.getCurrentFocus());
                    AppExtensions.clearError(assistantPhone_Input);
                }
            }
        });

        addTiming_Button.setOnClickListener(v ->
                AddTimingFragment.show()
                        .setOnTimingListener(new AddTimingFragment.OnTimingListener() {
                            @Override
                            public void onAdd(Timing timing) {
                                timings.add(timing);
                                timing_Adapter.setTimings(timings);
                            }

                            @Override
                            public void onEdit(Timing timing) {}

                            @Override
                            public void onRemove() {}
                        })
        );

        add_Button.setOnClickListener(view -> {
            String address = Objects.requireNonNull(chamberAddress_Input.getText()).toString().trim();
            String name = Objects.requireNonNull(assistantName_Input.getText()).toString().trim();
            String phone = Objects.requireNonNull(assistantPhone_Input.getText()).toString().trim();

            if (!AppExtensions.isInputValid(chamberAddress_Input, R.string.chamberAddress_Error)
            || (!TextUtils.isEmpty(phone) && !AppExtensions.isNumberValid(assistantPhone_Input, R.string.phone_Error))) return;
            if(timings.isEmpty()) {
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.addTimingsMessage, R.string.okay, CustomSnackBar.Duration.SHORT).show();
                return;
            }

            loading = LoadingFragment.show();

            AppointmentTiming appointmentTiming = new AppointmentTiming();
            appointmentTiming.setId(UUID.randomUUID().toString());
            appointmentTiming.setChamberAddress(address);
            if(!TextUtils.isEmpty(name)) appointmentTiming.setAssistantName(name);
            if(!TextUtils.isEmpty(phone)) appointmentTiming.setAssistantPhone(AppExtensions.getValidateNumber(assistantPhone_Input));
            appointmentTiming.setTimings(timings);

            addAppointmentTiming(appointmentTiming);
        });
    }

    private void setAdapter(){
        timing_Adapter = new TimingAdapter();
        rcv_Timings.setAdapter(timing_Adapter);
        timing_Adapter.setTimings(timings);
        timing_Adapter.setOnTimingListener(new TimingAdapter.OnTimingListener() {
            @Override
            public void onEdit(int position, Timing timing) {
                AddTimingFragment.show(timing)
                        .setOnTimingListener(new AddTimingFragment.OnTimingListener() {
                            @Override
                            public void onAdd(Timing timing) {}

                            @Override
                            public void onEdit(Timing timing) {
                                timings.set(position, timing);
                                timing_Adapter.setTimings(timings);
                            }

                            @Override
                            public void onRemove() {
                                timings.remove(position);
                                timing_Adapter.setTimings(timings);
                            }
                        });
            }

            @Override
            public void onRemove(int position, Timing timing) {
                AlertDialogFragment.show(R.string.removeTiming, (AppExtensions.getString(R.string.youWantToRemove) + " " + AppExtensions.getTiming(timing).get("timing")), R.string.cancel, R.string.remove)
                        .setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
                            @Override
                            public void onLeftButtonClick() {}

                            @Override
                            public void onRightButtonClick() {
                                timings.remove(position);
                                timing_Adapter.setTimings(timings);
                            }
                        });
            }
        });
    }

    private void addAppointmentTiming(AppointmentTiming appointmentTiming){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reference = db
                .collection(FirebaseHelper.USERS_TABLE)
                .document(LocalStorage.USER.getId());

        firebaseHelper.setDocumentData(reference.update(User.TIMINGS, FieldValue.arrayUnion(appointmentTiming)),
                new FirebaseHelper.OnFirebaseUpdateListener() {
                    @Override
                    public void onSuccess() {
                        AppExtensions.dismissLoading(loading);
                        if(mOnAppointmentTimingListener != null) mOnAppointmentTimingListener.onAdd(appointmentTiming);
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

    public void setOnAppointmentTimingListener(OnAppointmentTimingListener onAppointmentTimingListener) {
        this.mOnAppointmentTimingListener = onAppointmentTimingListener;
    }

    public interface OnAppointmentTimingListener {
        void onAdd(AppointmentTiming appointmentTiming);
    }

    /**
     *  Context Bind
     **/
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
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
