package com.telemedicine.telecare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.telemedicine.telecare.R;
import com.telemedicine.telecare.model.food.FoodAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodsAmountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FoodAmount>     foodAmounts = new ArrayList<>();
    private OnFoodAmountListener mOnFoodAmountListener;

    public FoodsAmountAdapter() {}

    public void setFoodAmounts(List<FoodAmount> foodAmounts) {
        this.foodAmounts = foodAmounts == null || foodAmounts.isEmpty() ? new ArrayList<>() : foodAmounts;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private FoodAmount foodAmount(int position){
        return foodAmounts.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new FoodAmountViewHolder(layoutInflater.inflate(R.layout.sample_food_amount, parent, false));
    }

    private static class FoodAmountViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView     name;
        private final AppCompatTextView     quantity;
        private final AppCompatImageButton  editButton;
        private final AppCompatImageButton  removeButton;

        private FoodAmountViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.food_Name_Tv);
            quantity = v.findViewById(R.id.food_Quantity_Tv);
            editButton = v.findViewById(R.id.food_Edit_Button);
            removeButton = v.findViewById(R.id.food_Remove_Button);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof FoodAmountViewHolder){
            FoodAmountViewHolder viewHolder = (FoodAmountViewHolder) holder;

            viewHolder.name.setText(foodAmount(position).getFoodName());
            viewHolder.quantity.setText(String.format(Locale.getDefault(), "%.1f %s", foodAmount(position).getQuantity(), foodAmount(position).getQuantityUnit()));

            viewHolder.editButton.setOnClickListener(view -> {
                if(mOnFoodAmountListener != null) mOnFoodAmountListener.onEdit(position, foodAmount(position));
            });

            viewHolder.removeButton.setOnClickListener(view -> {
                if(mOnFoodAmountListener != null) mOnFoodAmountListener.onRemove(position, foodAmount(position));
            });
        }
    }

    @Override
    public int getItemCount() {
        return foodAmounts.size();
    }

    public void setOnFoodAmountListener(OnFoodAmountListener onFoodAmountListener) {
        this.mOnFoodAmountListener = onFoodAmountListener;
    }

    public interface OnFoodAmountListener {
        void onEdit(int position, FoodAmount foodAmount);
        void onRemove(int position, FoodAmount foodAmount);
    }
}
