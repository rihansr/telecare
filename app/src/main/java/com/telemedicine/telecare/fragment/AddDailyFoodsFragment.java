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
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.adapter.FoodsAmountAdapter;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.model.food.DailyFood;
import com.telemedicine.telecare.model.food.FoodAmount;
import com.telemedicine.telecare.util.CustomPopup;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@SuppressLint("SetTextI18n, ClickableViewAccessibility")
public class AddDailyFoodsFragment extends DialogFragment {

    private static final String     TAG = AddDailyFoodsFragment.class.getSimpleName();
    private Activity                activity;
    private AppCompatEditText       timing_Input;
    private RecyclerView            rcv_Foods;
    private FoodsAmountAdapter      foodsAmount_Adapter;
    private AppCompatImageButton    addAmount_Button;
    private AppCompatButton         add_Button;
    private AppCompatImageView      back_Button;
    private List<FoodAmount>        foodAmounts;

    private LoadingFragment         loading;
    private FirebaseHelper          firebaseHelper;

    public static AddDailyFoodsFragment show(){
        AddDailyFoodsFragment fragment = new AddDailyFoodsFragment();
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
        return inflater.inflate(R.layout.fragment_layout_add_daily_food, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initId(view);

        init();
    }

    private void initId(View view) {
        timing_Input = view.findViewById(R.id.dailyFoods_Timing_Input);
        timing_Input.setInputType(InputType.TYPE_NULL);
        rcv_Foods = view.findViewById(R.id.dailyFoods_Foods_Rcv);
        addAmount_Button = view.findViewById(R.id.dailyFoods_AddAmount_Button);
        add_Button = view.findViewById(R.id.dailyFoods_Add_Button);
        back_Button = view.findViewById(R.id.dailyFoods_Back_Button);
    }

    private void init() {
        firebaseHelper = new FirebaseHelper();

        foodAmounts = new ArrayList<>();

        setAdapter();

        back_Button.setOnClickListener(view -> dismiss());

        timing_Input.setOnTouchListener((v, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                new CustomPopup(v, AppExtensions.getStringArray(R.array.mealTiming), CustomPopup.Popup.WINDOW)
                        .setOnPopupListener((position, item) -> {
                            timing_Input.setText(item);
                            AppExtensions.clearError(v);
                        });
            }
            return false;
        });

        addAmount_Button.setOnClickListener(v ->
                AddFoodAmountFragment.show()
                        .setOnFoodAmountListener(new AddFoodAmountFragment.OnFoodAmountListener() {
                            @Override
                            public void onAdd(FoodAmount foodAmount) {
                                foodAmounts.add(foodAmount);
                                foodsAmount_Adapter.setFoodAmounts(foodAmounts);
                            }

                            @Override
                            public void onEdit(FoodAmount foodAmount) {}

                            @Override
                            public void onRemove() {}
                        })
        );

        add_Button.setOnClickListener(view -> {
            String timing = Objects.requireNonNull(timing_Input.getText()).toString().trim();

            if (!AppExtensions.isInputValid(timing_Input, R.string.timing_Error)) return;
            if(foodAmounts.isEmpty()) {
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.addFoodsMessage, R.string.okay, CustomSnackBar.Duration.SHORT).show();
                return;
            }

            loading = LoadingFragment.show();

            DailyFood dailyFood = setupDailyFoods(timing);

            addDailyFoods(dailyFood, timing);
        });
    }

    private void setAdapter(){
        foodsAmount_Adapter = new FoodsAmountAdapter();
        rcv_Foods.setAdapter(foodsAmount_Adapter);
        foodsAmount_Adapter.setFoodAmounts(foodAmounts);
        foodsAmount_Adapter.setOnFoodAmountListener(new FoodsAmountAdapter.OnFoodAmountListener() {
            @Override
            public void onEdit(int position, FoodAmount foodAmount) {
                AddFoodAmountFragment.show(foodAmount)
                        .setOnFoodAmountListener(new AddFoodAmountFragment.OnFoodAmountListener() {
                            @Override
                            public void onAdd(FoodAmount foodAmount) {}

                            @Override
                            public void onEdit(FoodAmount foodAmount) {
                                foodAmounts.set(position, foodAmount);
                                foodsAmount_Adapter.setFoodAmounts(foodAmounts);
                            }

                            @Override
                            public void onRemove() {
                                foodAmounts.remove(position);
                                foodsAmount_Adapter.setFoodAmounts(foodAmounts);
                            }
                        });
            }

            @Override
            public void onRemove(int position, FoodAmount foodAmount) {
                AlertDialogFragment.show(R.string.removeFood, (AppExtensions.getString(R.string.youWantToRemove) + " " + foodAmount.getFoodName()), R.string.cancel, R.string.remove)
                        .setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
                            @Override
                            public void onLeftButtonClick() {}

                            @Override
                            public void onRightButtonClick() {
                                foodAmounts.remove(position);
                                foodsAmount_Adapter.setFoodAmounts(foodAmounts);
                            }
                        });
            }
        });
    }

    private void addDailyFoods(DailyFood food, String timing){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference foodsReference = db.collection(FirebaseHelper.DAILY_FOODS_TABLE)
                .document(LocalStorage.USER.getId())
                .collection(FirebaseHelper.FOODS_COLLECTION)
                .document(timing);

        firebaseHelper.setDocumentData(foodsReference.set(food),
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

    private DailyFood setupDailyFoods(String timing){
        DailyFood dailyFood = new DailyFood();
        dailyFood.setTiming(timing);
        dailyFood.setDate(new Date());
        dailyFood.setFoods(new ArrayList<>());

        for(FoodAmount foodAmount : foodAmounts){
            dailyFood.setTotalCalories(addValue(dailyFood.getTotalCalories(), amountCalculate(foodAmount.getFood().getQuantity(), foodAmount.getQuantity(), foodAmount.getFood().getCalories())));
            dailyFood.setTotalFat(addValue(dailyFood.getTotalFat(), amountCalculate(foodAmount.getFood().getQuantity(), foodAmount.getQuantity(), foodAmount.getFood().getFat())));
            dailyFood.setTotalCarboHydrate(addValue(dailyFood.getTotalCarboHydrate(), amountCalculate(foodAmount.getFood().getQuantity(), foodAmount.getQuantity(), foodAmount.getFood().getCarboHydrate())));
            dailyFood.setTotalSugars(addValue(dailyFood.getTotalSugars(), amountCalculate(foodAmount.getFood().getQuantity(), foodAmount.getQuantity(), foodAmount.getFood().getSugars())));
            dailyFood.setTotalProtein(addValue(dailyFood.getTotalProtein(), amountCalculate(foodAmount.getFood().getQuantity(), foodAmount.getQuantity(), foodAmount.getFood().getProtein())));
            dailyFood.setTotalVitaminA(addValue(dailyFood.getTotalVitaminA(), amountCalculate(foodAmount.getFood().getQuantity(), foodAmount.getQuantity(), foodAmount.getFood().getVitaminA())));
            dailyFood.setTotalVitaminC(addValue(dailyFood.getTotalVitaminC(), amountCalculate(foodAmount.getFood().getQuantity(), foodAmount.getQuantity(), foodAmount.getFood().getVitaminC())));
            dailyFood.setTotalCalcium(addValue(dailyFood.getTotalCalcium(), amountCalculate(foodAmount.getFood().getQuantity(), foodAmount.getQuantity(), foodAmount.getFood().getCalcium())));
            dailyFood.setTotalSaturatedFat(addValue(dailyFood.getTotalSaturatedFat(), amountCalculate(foodAmount.getFood().getQuantity(), foodAmount.getQuantity(), foodAmount.getFood().getSaturatedFat())));
            dailyFood.setTotalCholesterol(addValue(dailyFood.getTotalCholesterol(), amountCalculate(foodAmount.getFood().getQuantity(), foodAmount.getQuantity(), foodAmount.getFood().getCholesterol())));
            foodAmount.setFood(null);
            dailyFood.getFoods().add(foodAmount);
        }

        return dailyFood;
    }

    Double addValue(Double totalValue, Double value){
        double finalValue = (totalValue == null ? 0 : totalValue) + value;
        return finalValue == 0 ? null : finalValue;
    }

    Double amountCalculate(Double oldQuantity, Double newQuantity, Double value){
        if(oldQuantity == null || oldQuantity == 0 || newQuantity == null || newQuantity == 0 || value == null || value == 0) return 0.0;
        else return ((newQuantity / oldQuantity) * value);
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
