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
import com.telemedicine.telecare.model.food.DailyFood;
import com.telemedicine.telecare.model.food.FoodAmount;
import com.telemedicine.telecare.util.extensions.AppExtensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DailyFoodsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DailyFood> dailyFoods = new ArrayList<>();
    private OnFoodListener  mOnFoodListener;

    public DailyFoodsAdapter() {}

    public void setDailyFoods(List<DailyFood> dailyFoods) {
        this.dailyFoods = dailyFoods == null || dailyFoods.isEmpty() ? new ArrayList<>() : dailyFoods;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private DailyFood dailyFood(int position){
        return dailyFoods.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new DailyFoodsViewHolder(layoutInflater.inflate(R.layout.sample_daily_food, parent, false));
    }

    private static class DailyFoodsViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView     shift;
        private final AppCompatTextView     foods;
        private final AppCompatTextView     totalInfo;
        private final AppCompatImageButton  removeButton;

        private DailyFoodsViewHolder(View v) {
            super(v);
            shift = v.findViewById(R.id.foods_Shift_Tv);
            foods = v.findViewById(R.id.foods_Names_Tv);
            totalInfo = v.findViewById(R.id.foods_TotalInfo_Tv);
            removeButton = v.findViewById(R.id.foods_Remove_Button);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof DailyFoodsViewHolder){
            DailyFoodsViewHolder viewHolder = (DailyFoodsViewHolder) holder;

            viewHolder.shift.setText(dailyFood(position).getTiming());

            viewHolder.foods.setText(getFoodNames(dailyFood(position).getFoods()));

            String calories = AppExtensions.decimalFormat(dailyFood(position).getTotalCalories(), "#.##", false, "");
            String fat = AppExtensions.decimalFormat(dailyFood(position).getTotalFat(), "#.##", false, "");
            String carboHydrate = AppExtensions.decimalFormat(dailyFood(position).getTotalCarboHydrate(), "#.##", false, "");
            String sugars = AppExtensions.decimalFormat(dailyFood(position).getTotalSugars(), "#.##", false, "");
            String protein = AppExtensions.decimalFormat(dailyFood(position).getTotalProtein(), "#.##", false, "");
            String vitaminA = AppExtensions.decimalFormat(dailyFood(position).getTotalVitaminA(), "#.##", false, "");
            String vitaminC = AppExtensions.decimalFormat(dailyFood(position).getTotalVitaminC(), "#.##", false, "");
            String calcium = AppExtensions.decimalFormat(dailyFood(position).getTotalCalcium(), "#.##", false, "");
            String saturatedFat = AppExtensions.decimalFormat(dailyFood(position).getTotalSaturatedFat(), "#.##", false, "");
            String cholesterol = AppExtensions.decimalFormat(dailyFood(position).getTotalCholesterol(), "#.##", false, "");

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
            viewHolder.totalInfo.setVisibility(TextUtils.isEmpty(stringBuilder.toString()) ? View.GONE : View.VISIBLE);
            viewHolder.totalInfo.setText(stringBuilder.toString());

            viewHolder.itemView.setOnClickListener(view -> {
                if(mOnFoodListener != null) mOnFoodListener.onSelect(dailyFood(position));
            });

            viewHolder.removeButton.setOnClickListener(view -> {
                if(mOnFoodListener != null) mOnFoodListener.onRemove(dailyFood(position));
            });
        }
    }

    public static String getFoodNames(List<FoodAmount> list){
        if(list == null || list.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).getFoodName());
            if (i != (list.size() - 1)) sb.append(", ");
        }

        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return dailyFoods.size();
    }

    public void setOnFoodListener(OnFoodListener onFoodListener) {
        this.mOnFoodListener = onFoodListener;
    }

    public interface OnFoodListener {
        void onSelect(DailyFood dailyFood);
        void onRemove(DailyFood dailyFood);
    }
}
