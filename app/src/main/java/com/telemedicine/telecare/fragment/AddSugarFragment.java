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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.model.other.DailyData;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.telemedicine.telecare.util.extensions.DateExtensions;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@SuppressLint("SetTextI18n, ClickableViewAccessibility")

public class AddSugarFragment extends DialogFragment {

    private static final String         TAG = AddSugarFragment.class.getSimpleName();
    private Activity                    activity;
    private AppCompatEditText           date_Input;
    private AppCompatEditText           level_input;
    private AppCompatButton             add_Button;
    private AppCompatImageView          back_Button;
    private LoadingFragment             loading;
    private OnSugarLevelChangeListener  mOnSugarLevelChangeListener;

    public static AddSugarFragment show(){
        AddSugarFragment fragment = new AddSugarFragment();
        fragment.show(((AppCompatActivity) AppController.getActivity()).getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        return inflater.inflate(R.layout.fragment_layout_add_sugar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initId(view);

        init();
    }

    private void initId(View view) {
        date_Input = view.findViewById(R.id.sugar_Date_Input);
        date_Input.setInputType(InputType.TYPE_NULL);
        level_input = view.findViewById(R.id.sugar_Level_Input);
        add_Button = view.findViewById(R.id.sugar_Add_Button);
        back_Button = view.findViewById(R.id.sugar_Back_Button);
    }

    private void init() {
        back_Button.setOnClickListener(view -> dismiss());

        date_Input.setText(new DateExtensions(System.currentTimeMillis()).defaultDateFormat());

        date_Input.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                DateExtensions.showDatePicker(date_Input);
            }
            return false;
        });

        boolean isToday = LocalStorage.USER.getSugarLevel() != null
                && LocalStorage.USER.getSugarLevel().getDate() != null
                && new DateExtensions().isToday(LocalStorage.USER.getSugarLevel().getDate());

        level_input.setText(LocalStorage.USER.getSugarLevel() == null || LocalStorage.USER.getSugarLevel().getValue() == null || !isToday
                ? ""
                : String.format(Locale.getDefault(), "%d", LocalStorage.USER.getSugarLevel().getValue()));

        add_Button.setOnClickListener(view -> {
            String date = Objects.requireNonNull(date_Input.getText()).toString().trim();
            String level = Objects.requireNonNull(level_input.getText()).toString().trim();

            if (!AppExtensions.isInputValid(date_Input, R.string.todayDate_Error)
                    || !AppExtensions.isInputValid(level_input, R.string.sugarLevel_Error)
            ) return;

            DailyData dailyData = new DailyData();
            dailyData.setDate(new Date(new DateExtensions(date).getDate()));
            dailyData.setValue(Integer.parseInt(level));

            updateData(dailyData);
        });
    }

    private void updateData(DailyData dailyData){
        loading = LoadingFragment.show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference deviceReference = db.collection(FirebaseHelper.USERS_TABLE).document(LocalStorage.USER.getId());
        new FirebaseHelper().setDocumentData(deviceReference.update(User.SUGAR_LEVEL, dailyData),
                new FirebaseHelper.OnFirebaseUpdateListener() {
                    @Override
                    public void onSuccess() {
                        LocalStorage.USER.setSugarLevel(dailyData);
                        LocalStorage.setUserInfo(LocalStorage.USER, true);
                        if(mOnSugarLevelChangeListener != null) mOnSugarLevelChangeListener.onChange();
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

    public void setOnChangeListener(OnSugarLevelChangeListener mOnSugarLevelChangeListener) {
        this.mOnSugarLevelChangeListener = mOnSugarLevelChangeListener;
    }

    public interface OnSugarLevelChangeListener {
        void onChange();
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
