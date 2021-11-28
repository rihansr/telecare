package com.telemedicine.telecare.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.fragment.PhotoViewFragment;
import com.telemedicine.telecare.model.food.Food;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.telemedicine.telecare.wiget.CircleImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Food>     foods = new ArrayList<>();
    private OnFoodListener mOnFoodListener;

    public FoodsAdapter() {}

    public void setFoods(List<Food> foods) {
        this.foods = foods == null || foods.isEmpty() ? new ArrayList<>() : foods;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private Food food(int position){
        return foods.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new FoodViewHolder(layoutInflater.inflate(R.layout.sample_food, parent, false));
    }

    private static class FoodViewHolder extends RecyclerView.ViewHolder {

        private final CircleImageView       icon;
        private final AppCompatTextView     name;
        private final AppCompatTextView     quantity;
        private final AppCompatTextView     type;
        private final AppCompatTextView     additionalInfo;
        private final AppCompatImageButton  editButton;
        private final AppCompatImageButton  removeButton;

        private FoodViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.food_Icon_Iv);
            name = v.findViewById(R.id.food_Name_Tv);
            quantity = v.findViewById(R.id.food_Quantity_Tv);
            type = v.findViewById(R.id.food_FoodType_Tv);
            additionalInfo = v.findViewById(R.id.food_AdditionalInfo_Tv);
            editButton = v.findViewById(R.id.food_Edit_Button);
            removeButton = v.findViewById(R.id.food_Remove_Button);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof FoodViewHolder){
            FoodViewHolder viewHolder = (FoodViewHolder) holder;

            AppExtensions.loadPhoto(viewHolder.icon, food(position).getPhoto(), R.dimen.icon_Size_X_Large, R.drawable.ic_placeholder);
            viewHolder.icon.setOnClickListener(view -> PhotoViewFragment.show(food(position).getPhoto()));

            viewHolder.name.setText(food(position).getName());

            String quantity = AppExtensions.decimalFormat(food(position).getQuantity(), "#.#", false, AppExtensions.getString(R.string.nullSymbol));
            viewHolder.quantity.setText(String.format(Locale.getDefault(), "%s%s", quantity, food(position).getQuantityUnit()));

            viewHolder.type.setText(food(position).getType());

            String calories = AppExtensions.decimalFormat(food(position).getCalories(), "#.##", false, "");
            String fat = AppExtensions.decimalFormat(food(position).getFat(), "#.##", false, "");
            String carboHydrate = AppExtensions.decimalFormat(food(position).getCarboHydrate(), "#.##", false, "");
            String sugars = AppExtensions.decimalFormat(food(position).getSugars(), "#.##", false, "");
            String protein = AppExtensions.decimalFormat(food(position).getProtein(), "#.##", false, "");
            String vitaminA = AppExtensions.decimalFormat(food(position).getVitaminA(), "#.##", false, "");
            String vitaminC = AppExtensions.decimalFormat(food(position).getVitaminC(), "#.##", false, "");
            String calcium = AppExtensions.decimalFormat(food(position).getCalcium(), "#.##", false, "");
            String saturatedFat = AppExtensions.decimalFormat(food(position).getSaturatedFat(), "#.##", false, "");
            String cholesterol = AppExtensions.decimalFormat(food(position).getCholesterol(), "#.##", false, "");
            String additional = food(position).getAdditionalInfo() == null ? "" : food(position).getAdditionalInfo();

            StringBuilder stringBuilder = new StringBuilder();
            if(!TextUtils.isEmpty(calories)) stringBuilder.append(String.format(Locale.getDefault(), "%s\t-\t%s\t", AppExtensions.getString(R.string.calories), calories));
            if(!TextUtils.isEmpty(fat)) stringBuilder.append(String.format(Locale.getDefault(), "|\t%s\t-\t%s%s\t", AppExtensions.getString(R.string.fat), fat, AppExtensions.getString(R.string.g)));
            if(!TextUtils.isEmpty(carboHydrate)) stringBuilder.append(String.format(Locale.getDefault(), "|\t%s\t-\t%s%s\t", AppExtensions.getString(R.string.carboHydrate), carboHydrate, AppExtensions.getString(R.string.g)));
            if(!TextUtils.isEmpty(sugars)) stringBuilder.append(String.format(Locale.getDefault(), "|\t%s\t-\t%s%s\t", AppExtensions.getString(R.string.sugars), sugars, AppExtensions.getString(R.string.g)));
            if(!TextUtils.isEmpty(protein)) stringBuilder.append(String.format(Locale.getDefault(), "|\t%s\t-\t%s%s\t", AppExtensions.getString(R.string.protein), sugars, AppExtensions.getString(R.string.g)));
            if(!TextUtils.isEmpty(vitaminA)) stringBuilder.append(String.format(Locale.getDefault(), "|\t%s\t-\t%s%s\t", AppExtensions.getString(R.string.vitaminA), vitaminA, AppExtensions.getString(R.string.percent)));
            if(!TextUtils.isEmpty(vitaminC)) stringBuilder.append(String.format(Locale.getDefault(), "|\t%s\t-\t%s%s\t", AppExtensions.getString(R.string.vitaminC), vitaminC, AppExtensions.getString(R.string.percent)));
            if(!TextUtils.isEmpty(calcium)) stringBuilder.append(String.format(Locale.getDefault(), "|\t%s\t-\t%s%s\t", AppExtensions.getString(R.string.calcium), calcium, AppExtensions.getString(R.string.percent)));
            if(!TextUtils.isEmpty(saturatedFat)) stringBuilder.append(String.format(Locale.getDefault(), "|\t%s\t-\t%s%s\t", AppExtensions.getString(R.string.saturatedFat), saturatedFat, AppExtensions.getString(R.string.percent)));
            if(!TextUtils.isEmpty(cholesterol)) stringBuilder.append(String.format(Locale.getDefault(), "|\t%s\t-\t%s%s\t", AppExtensions.getString(R.string.cholesterol), cholesterol, AppExtensions.getString(R.string.mg)));
            if(!TextUtils.isEmpty(additional)) stringBuilder.append(String.format(Locale.getDefault(), "|\t%s\t-\t%s", AppExtensions.getString(R.string.additional), additional));
            viewHolder.additionalInfo.setVisibility(TextUtils.isEmpty(stringBuilder.toString()) ? View.GONE : View.VISIBLE);
            viewHolder.additionalInfo.setText(stringBuilder.toString());

            viewHolder.editButton.setOnClickListener(view -> {
                if(mOnFoodListener != null) mOnFoodListener.onSelect(food(position));
            });

            viewHolder.removeButton.setOnClickListener(view -> {
                if(mOnFoodListener != null) mOnFoodListener.onRemove(food(position));
            });
        }
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public void setOnFoodListener(OnFoodListener onFoodListener) {
        this.mOnFoodListener = onFoodListener;
    }

    public interface OnFoodListener {
        void onSelect(Food food);
        void onRemove(Food food);
    }
}
