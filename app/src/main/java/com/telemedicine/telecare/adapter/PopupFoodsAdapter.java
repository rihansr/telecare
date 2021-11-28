package com.telemedicine.telecare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.telemedicine.telecare.R;
import com.telemedicine.telecare.fragment.PhotoViewFragment;
import com.telemedicine.telecare.model.food.Food;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.telemedicine.telecare.wiget.CircleImageView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PopupFoodsAdapter extends ArrayAdapter {

    private final List<Food> foods;
    private final List<Food> allFoods;

    public PopupFoodsAdapter(@NonNull Context context, List<Food> foods) {
        super(context, 0, ((foods == null || foods.isEmpty()) ? new ArrayList<>() : foods));
        this.foods = (foods == null || foods.isEmpty()) ? new ArrayList<>() : foods;
        this.allFoods = new ArrayList<>(this.foods);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return itemsFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull final ViewGroup parent) {
        if(view == null) view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_popup_food_item, parent, false);

        CircleImageView photo = view.findViewById(R.id.food_Icon_Iv);
        AppCompatTextView name = view.findViewById(R.id.food_Name_Tv);
        AppCompatTextView type = view.findViewById(R.id.food_Type_Tv);

        final Food food = (Food) getItem(position);

        AppExtensions.loadPhoto(photo, food.getPhoto(), R.dimen.icon_Size_Large, R.drawable.ic_placeholder);

        photo.setOnLongClickListener(v -> {
            PhotoViewFragment.show(food.getPhoto());
            return false;
        });

        name.setText(food.getName());

        type.setText(food.getType());

        return view;
    }

    private final Filter itemsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Food> filtered_Foods = new LinkedList<>();

            if(charSequence == null || charSequence.length() == 0){
                filtered_Foods.addAll(allFoods);
            }
            else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Food food : allFoods){
                    if(food.getName().trim().toLowerCase().contains(filterPattern)){
                        filtered_Foods.add(food);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filtered_Foods;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            foods.clear();
            foods.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Food) resultValue).getName();
        }
    };
}
