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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.adapter.PopupActivityLevelsAdapter;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.model.other.ActivityLevel;
import com.telemedicine.telecare.model.other.BMI;
import com.telemedicine.telecare.model.other.Limit;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.util.CustomPopup;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Objects;

@SuppressLint("ClickableViewAccessibility")
public class BmiCalculationFragment extends DialogFragment {

    private static final String     TAG = BmiCalculationFragment.class.getSimpleName();
    private Activity                activity;
    private Context                 context;
    private AppCompatEditText       height_Input;
    private AppCompatEditText       weight_Input;
    private AppCompatEditText       activityLevel_Input;
    private ActivityLevel           selectedActivityLevel;
    private AppCompatEditText       bmi_Input;
    private AppCompatTextView       bmiCategory_input;
    private AppCompatEditText       lbm_input;
    private AppCompatTextView       lbm_title;
    private AppCompatEditText       lfm_input;
    private AppCompatTextView       lfm_title;
    private AppCompatButton         calculate_Button;
    private AppCompatImageView      back_Button;
    private LoadingFragment         loading;
    private OnBmiChangeListener     mOnBmiChangeListener;

    public static BmiCalculationFragment show(){
        BmiCalculationFragment fragment = new BmiCalculationFragment();
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
        return inflater.inflate(R.layout.fragment_layout_bmi_calculation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initId(view);

        init();
    }

    private void initId(View view) {
        height_Input = view.findViewById(R.id.bmi_Height_Input);
        weight_Input = view.findViewById(R.id.bmi_Weight_Input);
        activityLevel_Input = view.findViewById(R.id.bmi_Activity_Input);
        activityLevel_Input.setInputType(InputType.TYPE_NULL);
        bmi_Input = view.findViewById(R.id.bmi_Value_Input);
        bmi_Input.setInputType(InputType.TYPE_NULL);
        bmiCategory_input = view.findViewById(R.id.bmi_Category_Input);
        bmiCategory_input.setInputType(InputType.TYPE_NULL);
        lbm_input = view.findViewById(R.id.bmi_LBM_Input);
        lbm_input.setInputType(InputType.TYPE_NULL);
        lbm_title = view.findViewById(R.id.bmi_LBM_Title);
        lfm_input = view.findViewById(R.id.bmi_LFM_Input);
        lfm_input.setInputType(InputType.TYPE_NULL);
        lfm_title = view.findViewById(R.id.bmi_LFM_Title);
        calculate_Button = view.findViewById(R.id.bmi_Calculate_Button);
        back_Button = view.findViewById(R.id.bmi_Back_Button);
    }

    private void init() {
        LocalStorage.getActivityLevels();

        back_Button.setOnClickListener(view -> dismiss());

        height_Input.setText(String.format(Locale.getDefault(), "%.1f", LocalStorage.USER.getHeight()));
        weight_Input.setText(String.format(Locale.getDefault(), "%.1f", LocalStorage.USER.getWeight()));

        selectedActivityLevel = LocalStorage.USER.getBmi() == null || LocalStorage.USER.getBmi().getActivityLevel() == null
                ? null
                : LocalStorage.USER.getBmi().getActivityLevel();

        activityLevel_Input.setText(selectedActivityLevel == null ? null : selectedActivityLevel.getLevel());

        DecimalFormat df = new DecimalFormat("###.#");
        bmi_Input.setText(LocalStorage.USER.getBmi() == null || LocalStorage.USER.getBmi().getValue() == null
                ? ""
                : (df.format(LocalStorage.USER.getBmi().getValue()) + " kg/m²"));
        bmiCategory_input.setText(LocalStorage.USER.getBmi() == null || LocalStorage.USER.getBmi().getCategory() == null
                ? ""
                : (LocalStorage.USER.getBmi().getCategory() + ""));
        lbm_input.setText(LocalStorage.USER.getBmi() == null || LocalStorage.USER.getBmi().getLbm() == null
                ? ""
                : (df.format(LocalStorage.USER.getBmi().getLbm()) + " " + AppExtensions.getString(R.string.kg_Hint)));
        lbm_title.setText(LocalStorage.USER.getBmi() == null || LocalStorage.USER.getBmi().getLbm() == null
                ? "" : "LBM");
        lfm_input.setText(LocalStorage.USER.getBmi() == null || LocalStorage.USER.getBmi().getLfm() == null
                ? ""
                : (df.format(LocalStorage.USER.getBmi().getLfm()) + " " + AppExtensions.getString(R.string.kg_Hint)));
        lfm_title.setText(LocalStorage.USER.getBmi() == null || LocalStorage.USER.getBmi().getLfm() == null
                ? "" : "LFM");

        activityLevel_Input.setOnTouchListener((v, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                new CustomPopup(v, new PopupActivityLevelsAdapter(context, LocalStorage.activityLevelLevels), 350, CustomPopup.Popup.WINDOW)
                        .setOnItemClickListener((parent, view, pos, id) -> {
                            selectedActivityLevel = (ActivityLevel) parent.getAdapter().getItem(pos);
                            activityLevel_Input.setText(selectedActivityLevel.getLevel());
                            AppExtensions.clearError(v);
                        });
            }
            return false;
        });

        calculate_Button.setOnClickListener(view -> {
            String height = Objects.requireNonNull(height_Input.getText()).toString().trim();
            String weight = Objects.requireNonNull(weight_Input.getText()).toString().trim();

            if (!AppExtensions.isInputValid(height_Input, R.string.height_Error)
                    || !AppExtensions.isInputValid(weight_Input, R.string.weight_Error)
                    || !AppExtensions.isInputValid(activityLevel_Input, selectedActivityLevel == null, R.string.activityLevel_Error)
            ) return;

            calculateBmi(Double.parseDouble(height), Double.parseDouble(weight));
        });
    }

    private void calculateBmi(double height, double weight) {
        /**
         * BMI = kg/m2
         **/
        double cm = convertToCm(height);
        double kg = weight * 1.0;
        double bmi = kg / Math.pow((cm / 100), 2);
        final Integer age = LocalStorage.USER.getAge();
        final String gender = LocalStorage.USER.getGender();
        final String category;
        final double lbm; // Lean Body Mass
        final double lfm; // Lean Fat Mass

        if (bmi >= 0 && bmi < 15) category = "Very Severely Underweight";
        else if (bmi >= 15 && bmi < 16) category = "Severely Underweight";
        else if (bmi >= 16 && bmi < 18.5) category = "Underweight";
        else if (bmi >= 18.5 && bmi < 25) category = "Healthy Weight (Normal)";
        else if (bmi >= 25 && bmi < 30) category = "Overweight";
        else if (bmi >= 30 && bmi < 35) category = "Moderately Obese";
        else if (bmi >= 35 && bmi < 40) category = "Severely Obese";
        else if (bmi >= 40 && bmi < 45) category = "Very Severely Obese";
        else if (bmi >= 45 && bmi < 50) category = "Morbidly Obese";
        else if (bmi >= 50 && bmi < 60) category = "Super Obese";
        else category = "Hyper Obese";

        /**
         * Boer Formula
         * Below 15: LBM = 0.0215 * weight [kg] ^ 0.6469 * height [cm] ^ 0.7236
         * Male: LBM = 0.407 * weight [kg] + 0.267 * height [cm] - 19.2
         * Female: LBM = 0.252 * weight [kg] + 0.473 * height [cm] - 48.3
         **/
        if (age <= 14) {
            double ECV = 0.0215 * (Math.pow(kg, 0.6469)) * (Math.pow(cm, 0.7236));
            lbm = 3.8 * ECV;
        } else {
            if (gender.equals("Male")) lbm = (0.407 * kg) + (0.267 * cm) - 19.2;
            else  lbm = (0.252 * kg) + (0.473 * cm) - 48.3;
        }
        lfm = kg - lbm;

        DecimalFormat df = new DecimalFormat("###.#");
        bmi_Input.setText(String.format(Locale.getDefault(), "%s %s", df.format(bmi), AppExtensions.getString(R.string.bmi)));
        bmiCategory_input.setText(category);
        lbm_input.setText(String.format(Locale.getDefault(), "%s %s", df.format(lbm), AppExtensions.getString(R.string.kg_Hint)));
        lbm_title.setText(AppExtensions.getString(R.string.lbm));
        lfm_input.setText(String.format(Locale.getDefault(), "%s %s", df.format(lfm), AppExtensions.getString(R.string.kg_Hint)));
        lfm_title.setText(AppExtensions.getString(R.string.lfm));

        BMI bmiData = new BMI();
        bmiData.setValue(bmi);
        bmiData.setCategory(category);
        bmiData.setLbm(lbm);
        bmiData.setLfm(lfm);
        bmiData.setActivityLevel(selectedActivityLevel);

        calculateBmr(height, weight, gender, age, bmiData);
    }

    private void calculateBmr(double height, double weight, String gender, int age, BMI bmi) {
        /**
         *  Mifflin-St Jeor Formula
         *  Male: BMR = 10W + 6.25H - 5A + 5
         *  Female: BMR = 10W + 6.25H - 5A - 161
         **/

        double cm = convertToCm(height);
        double kg = weight * 1.0;

        double v = (10 * kg) + (6.25 * cm) - (5 * age);
        double bmr = gender.equals("Male") ? (v + 5) : (v - 161);
        double calorie = bmr * selectedActivityLevel.getValue();

        bmi.setCalorie(new Limit(Math.round(Math.round(calorie * .73)), Math.round(Math.round(calorie * 1))));

        bmi.setCarbohydrate(new Limit(calculate(calorie, 45), calculate(calorie, 65)));

        if (age <= 3) {
            bmi.setProtein(new Limit(calculate(calorie, 5), calculate(calorie, 20)));
            bmi.setFat(new Limit(calculate(calorie, 30), calculate(calorie, 40)));
        }
        else if (age <= 18) {
            bmi.setProtein(new Limit(calculate(calorie, 10), calculate(calorie, 30)));
            bmi.setFat(new Limit(calculate(calorie, 25), calculate(calorie, 35)));
        }
        else {
            bmi.setProtein(new Limit(calculate(calorie, 10), calculate(calorie, 35)));
            bmi.setFat(new Limit(calculate(calorie, 20), calculate(calorie, 35)));
        }

        updateData(height, weight, bmi);
    }

    private double convertToCm(double height){
        if(height == 0) return 1;
        height = Double.parseDouble(String.valueOf(height));
        int feet = (int) Math.floor(height);
        int inch = Integer.parseInt(String.valueOf(height).split("\\.")[1]);
        return ((feet * 12) + inch) / 0.39370;
    }

    private long calculate(double bmr, int value){
        return Math.round(((bmr * value) / 100) * 0.12959782);
    }

    private void updateData(double height, double weight, BMI bmi){
        loading = LoadingFragment.show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference deviceReference = db.collection(FirebaseHelper.USERS_TABLE).document(LocalStorage.USER.getId());
        new FirebaseHelper().setDocumentData(deviceReference.update(User.HEIGHT, height, User.WEIGHT, weight, User.BMI, bmi),
                new FirebaseHelper.OnFirebaseUpdateListener() {
                    @Override
                    public void onSuccess() {
                        LocalStorage.USER.setHeight(height);
                        LocalStorage.USER.setWeight(weight);
                        LocalStorage.USER.setBmi(bmi);
                        LocalStorage.setUserInfo(LocalStorage.USER, true);
                        if(mOnBmiChangeListener != null) mOnBmiChangeListener.onChange();
                        AppExtensions.dismissLoading(loading);
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

    public void setOnChangeListener(OnBmiChangeListener mOnBmiChangeListener) {
        this.mOnBmiChangeListener = mOnBmiChangeListener;
    }

    public interface OnBmiChangeListener {
        void onChange();
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
