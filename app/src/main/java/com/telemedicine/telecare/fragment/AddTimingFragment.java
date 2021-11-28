package com.telemedicine.telecare.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.InputType;
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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.adapter.WeekdaysAdapter;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.model.appointment.Timing;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.CustomPopup;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.telemedicine.telecare.util.extensions.DateExtensions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressLint("ClickableViewAccessibility")
public class AddTimingFragment extends DialogFragment {

    private static final String  TAG = AddTimingFragment.class.getSimpleName();
    private Activity             activity;
    private RecyclerView         weekdays_Rcv;
    private WeekdaysAdapter      weekdays_Adapter;
    private AppCompatEditText    startTime_Input;
    private AppCompatEditText    endTime_Input;
    private AppCompatEditText    consultationTime_Input;
    private AppCompatButton      add_Button;
    private AppCompatImageView   back_Button;
    private AppCompatImageView   more_Button;
    private List<String>         selectedWeekdays = new ArrayList<>();
    private Timing               timing;
    private OnTimingListener     mOnTimingListener;

    public static AddTimingFragment show(){
        AddTimingFragment fragment = new AddTimingFragment();
        fragment.show(((AppCompatActivity) AppController.getActivity()).getSupportFragmentManager(), TAG);
        return fragment;
    }

    public static AddTimingFragment show(Timing timing){
        AddTimingFragment fragment = new AddTimingFragment();
        if(timing != null){
            Bundle args = new Bundle();
            args.putSerializable(Constants.TIMING_BUNDLE_KEY, timing);
            fragment.setArguments(args);
        }
        fragment.show(((AppCompatActivity) AppController.getActivity()).getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        AppExtensions.halfScreenDialog(getDialog());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_add_timing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initId(view);

        init();
    }

    private void initId(View view) {
        weekdays_Rcv = view.findViewById(R.id.appointment_Weekday_Rcv);
        startTime_Input = view.findViewById(R.id.appointment_StartTime_Input);
        startTime_Input.setInputType(InputType.TYPE_NULL);
        endTime_Input = view.findViewById(R.id.appointment_EndTime_Input);
        consultationTime_Input = view.findViewById(R.id.appointment_ConsultationTime_Input);
        endTime_Input.setInputType(InputType.TYPE_NULL);
        add_Button = view.findViewById(R.id.food_Add_Button);
        more_Button = view.findViewById(R.id.food_More_Button);
        back_Button = view.findViewById(R.id.food_Back_Button);
    }

    private void init() {
        if(getArguments() != null && getArguments().containsKey(Constants.TIMING_BUNDLE_KEY)) {
            timing = (Timing) getArguments().getSerializable(Constants.TIMING_BUNDLE_KEY);
            add_Button.setText(AppExtensions.getString(timing == null ? R.string.add : R.string.update));

            getArguments().remove(Constants.TIMING_BUNDLE_KEY);
        }

        back_Button.setOnClickListener(view -> dismiss());

        more_Button.setVisibility(timing != null ? View.VISIBLE : View.GONE);

        more_Button.setOnClickListener(v ->
                new CustomPopup(v, new String[]{AppExtensions.getString(R.string.delete)}, CustomPopup.Popup.MENU)
                        .setOnPopupListener((position, item) -> {
                            if (position == 0) {
                                if(mOnTimingListener != null) {
                                    mOnTimingListener.onRemove();
                                    dismiss();
                                }
                            }
                        })
        );

        setAdapter();

        LocalStorage.getWeekdays();

        selectedWeekdays = timing == null || timing.getWeekdays() == null || timing.getWeekdays().isEmpty()
                ? new ArrayList<>()
                : timing.getWeekdays();

        weekdays_Adapter.setWeekdays(LocalStorage.weekdays, selectedWeekdays);

        startTime_Input.setText(timing == null || timing.getStartTime() == null ? "" : timing.getStartTime());
        endTime_Input.setText(timing == null || timing.getEndTime() == null ? "" : timing.getEndTime());
        consultationTime_Input.setText(LocalStorage.USER.getConsultationTime() == null
                ? null
                : LocalStorage.USER.getConsultationTime().toString()
        );

        startTime_Input.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) DateExtensions.showTimePicker(view);
            return false;
        });

        endTime_Input.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) DateExtensions.showTimePicker(view);
            return false;
        });

        add_Button.setOnClickListener(view -> {
            String startTime = Objects.requireNonNull(startTime_Input.getText()).toString().trim();
            String endTime = Objects.requireNonNull(endTime_Input.getText()).toString().trim();
            String consultationTime = Objects.requireNonNull(consultationTime_Input.getText()).toString().trim();

            if(selectedWeekdays.isEmpty()){
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.weekdays_Error, R.string.okay, CustomSnackBar.Duration.SHORT).show();
                return;
            }
            if (!AppExtensions.isInputValid(startTime_Input, R.string.startTime_Error)
                    || !AppExtensions.isInputValid(endTime_Input, R.string.endTime_Error)
                    || !AppExtensions.isInputValid(consultationTime_Input, R.string.consultationTime_Error)
            ) return;

            boolean isUpdate = this.timing != null;

            Timing timing = new Timing();
            timing.setWeekdays(selectedWeekdays);
            timing.setStartTime(startTime);
            timing.setEndTime(endTime);
            timing.setConsultationTime(Integer.parseInt(consultationTime));

            if(mOnTimingListener != null){
                if(isUpdate) {
                    mOnTimingListener.onEdit(timing);
                    dismiss();
                }
                else {
                    mOnTimingListener.onAdd(timing);
                    updateUi();
                    new CustomSnackBar(
                            AppExtensions.getRootView(getDialog()),
                            (AppExtensions.getTiming(timing).get("timing") + " " + AppExtensions.getString(R.string.addSuccessfully)), R.string.okay,
                            CustomSnackBar.Duration.LONG
                    ).show();
                }
            }
        });
    }

    private void setAdapter(){
        weekdays_Adapter = new WeekdaysAdapter();
        weekdays_Rcv.setAdapter(weekdays_Adapter);
        weekdays_Adapter.setOnWeekdaysListener(weekdays -> {
            selectedWeekdays = weekdays;
            weekdays_Adapter.setSelectedWeekdays(selectedWeekdays);
        });
    }

    public void updateUi(){
        selectedWeekdays = new ArrayList<>();
        weekdays_Adapter.setSelectedWeekdays(selectedWeekdays);
        startTime_Input.setText(null);
        endTime_Input.setText(null);
        consultationTime_Input.setText(LocalStorage.USER.getConsultationTime() == null
                ? null
                : LocalStorage.USER.getConsultationTime().toString()
        );
    }

    public void setOnTimingListener(OnTimingListener onTimingListener) {
        this.mOnTimingListener = onTimingListener;
    }

    public interface OnTimingListener {
        void onAdd(Timing timing);
        void onEdit(Timing timing);
        void onRemove();
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
