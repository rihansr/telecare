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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PopupItemsAdapter<T> extends ArrayAdapter<T> {

    private final List<T> items;
    private final List<T> allItems;

    public PopupItemsAdapter(@NonNull Context context, List<T> items) {
        super(context, 0, ((items == null || items.isEmpty()) ? new ArrayList<>() : items));
        this.items = (items == null || items.isEmpty()) ? new ArrayList<>() : items;
        this.allItems = new ArrayList<>(this.items);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return itemsFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull final ViewGroup parent) {
        if(view == null) view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_popup_item, parent, false);

        AppCompatTextView itemName = view.findViewById(R.id.itemTv);

        final T field = getItem(position);

        if(field != null) itemName.setText(field.toString());

        return view;
    }

    private final Filter itemsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<T> filtered_Fields = new LinkedList<>();

            if(charSequence == null || charSequence.length() == 0){
                filtered_Fields.addAll(allItems);
            }
            else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (T field : allItems){
                    if(field.toString().trim().toLowerCase().contains(filterPattern)){
                        filtered_Fields.add(field);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filtered_Fields;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            items.clear();
            items.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((T) resultValue).toString();
        }
    };
}
