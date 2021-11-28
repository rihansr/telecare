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
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.adapter.PopupFoodsAdapter;
import com.telemedicine.telecare.base.AppBaseActivity;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.model.food.Food;
import com.telemedicine.telecare.model.food.FoodAmount;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.CustomPopup;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

@SuppressLint("SetTextI18n, ClickableViewAccessibility")
public class AddFoodAmountFragment extends DialogFragment implements AppBaseActivity.OnFoodListener {

    private static final String             TAG = AddFoodAmountFragment.class.getSimpleName();
    private Activity                        activity;
    private Context                         context;
    private AppCompatAutoCompleteTextView   food_Input;
    private AppCompatEditText               quantity_Input;
    private AppCompatEditText               quantityUnit_Input;
    private AppCompatButton                 add_Button;
    private AppCompatImageView              back_Button;
    private AppCompatImageView              more_Button;
    private FoodAmount                      foodAmount;
    private Food                            selectedFood;
    private OnFoodAmountListener            mOnFoodAmountListener;

    public static AddFoodAmountFragment show(){
        AddFoodAmountFragment fragment = new AddFoodAmountFragment();
        fragment.show(((AppCompatActivity) AppController.getActivity()).getSupportFragmentManager(), TAG);
        return fragment;
    }

    public static AddFoodAmountFragment show(FoodAmount foodAmount){
        AddFoodAmountFragment fragment = new AddFoodAmountFragment();
        if(foodAmount != null){
            Bundle args = new Bundle();
            args.putSerializable(Constants.FOOD_AMOUNT_BUNDLE_KEY, foodAmount);
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
        return inflater.inflate(R.layout.fragment_layout_add_food_amount, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initId(view);

        init();
    }

    private void initId(View view) {
        food_Input = view.findViewById(R.id.food_Name_Input);
        quantity_Input = view.findViewById(R.id.food_Quantity_Input);
        quantityUnit_Input = view.findViewById(R.id.food_QuantityUnit_Input);
        quantityUnit_Input.setInputType(InputType.TYPE_NULL);
        add_Button = view.findViewById(R.id.food_Add_Button);
        more_Button = view.findViewById(R.id.food_More_Button);
        back_Button = view.findViewById(R.id.food_Back_Button);
    }

    private void init() {
        if(getArguments() != null && getArguments().containsKey(Constants.FOOD_AMOUNT_BUNDLE_KEY)) {
            foodAmount = (FoodAmount) getArguments().getSerializable(Constants.FOOD_AMOUNT_BUNDLE_KEY);
            add_Button.setText(AppExtensions.getString(foodAmount == null ? R.string.add : R.string.update));

            getArguments().remove(Constants.FOOD_AMOUNT_BUNDLE_KEY);
        }

        ((AppBaseActivity) activity).getFoods();
        ((AppBaseActivity) activity).setOnFoodListener(this);

        food_Input.setDropDownBackgroundDrawable(AppExtensions.getDrawable(R.drawable.shape_popup));

        selectedFood = foodAmount == null || foodAmount.getFood() == null ? null : foodAmount.getFood();

        back_Button.setOnClickListener(view -> dismiss());

        more_Button.setVisibility(foodAmount != null ? View.VISIBLE : View.GONE);

        more_Button.setOnClickListener(v ->
                new CustomPopup(v, new String[]{AppExtensions.getString(R.string.delete)}, CustomPopup.Popup.MENU)
                        .setOnPopupListener((position, item) -> {
                            if (position == 0) {
                                if(mOnFoodAmountListener != null) {
                                    mOnFoodAmountListener.onRemove();
                                    dismiss();
                                }
                            }
                        })
        );

        DecimalFormat df = new DecimalFormat("###.#");

        food_Input.setText(foodAmount == null || foodAmount.getFoodName() == null ? "" : foodAmount.getFoodName());

        food_Input.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus){
                if(selectedFood == null) food_Input.setText(null);
                else food_Input.setText(selectedFood.getName());
            }
        });

        food_Input.setThreshold(1);
        food_Input.setAdapter(new PopupFoodsAdapter(context, LocalStorage.foods));

        food_Input.setOnItemClickListener((parent, view, position, id) -> {
            selectedFood = (Food) parent.getAdapter().getItem(position);
            food_Input.setText(selectedFood.getName());
            quantityUnit_Input.setText(selectedFood.getQuantityUnit() == null ? "g" : selectedFood.getQuantityUnit());
            AppExtensions.clearError(view);
        });

        quantity_Input.setText(foodAmount == null || foodAmount.getQuantity() == null ? "" : df.format(foodAmount.getQuantity()));

        quantityUnit_Input.setText(foodAmount == null || foodAmount.getQuantityUnit() == null ? "" : foodAmount.getQuantityUnit());

        add_Button.setOnClickListener(view -> {
            String quantity = Objects.requireNonNull(quantity_Input.getText()).toString().trim();
            String quantityUnit = Objects.requireNonNull(quantityUnit_Input.getText()).toString().trim();

            if (!AppExtensions.isInputValid(food_Input, selectedFood == null, R.string.food_Error)
                    || !AppExtensions.isInputValid(quantity_Input, R.string.foodQuantity_Error)
            ) return;

            boolean isUpdate = this.foodAmount != null;

            FoodAmount foodAmount = new FoodAmount();
            foodAmount.setFoodId(selectedFood.getId());
            foodAmount.setFoodName(selectedFood.getName());
            foodAmount.setFood(selectedFood);
            foodAmount.setQuantity(Double.parseDouble(quantity));
            foodAmount.setQuantityUnit(quantityUnit);

            if(mOnFoodAmountListener != null){
                if(isUpdate) {
                    mOnFoodAmountListener.onEdit(foodAmount);
                    dismiss();
                }
                else {
                    mOnFoodAmountListener.onAdd(foodAmount);
                    updateUi();
                    new CustomSnackBar(AppExtensions.getRootView(getDialog()), (foodAmount.getFoodName() + " " + AppExtensions.getString(R.string.addSuccessfully)), R.string.okay, CustomSnackBar.Duration.LONG).show();
                }
            }
        });
    }

    public void updateUi(){
        selectedFood = null;
        food_Input.setText(null);
        quantity_Input.setText(null);
        quantityUnit_Input.setText(null);
    }

    @Override
    public void notifyFoods(List<Food> foods) {
        food_Input.setAdapter(new PopupFoodsAdapter(context, foods));
    }

    public void setOnFoodAmountListener(OnFoodAmountListener onFoodAmountListener) {
        this.mOnFoodAmountListener = onFoodAmountListener;
    }

    public interface OnFoodAmountListener {
        void onAdd(FoodAmount foodAmount);
        void onEdit(FoodAmount foodAmount);
        void onRemove();
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
