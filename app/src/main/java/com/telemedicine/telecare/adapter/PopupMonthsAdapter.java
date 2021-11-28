package com.telemedicine.telecare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.telemedicine.telecare.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PopupMonthsAdapter extends ArrayAdapter {

    private final List<Object[]> months;

    public PopupMonthsAdapter(@NonNull Context context, List<Object[]> months) {
        super(context, 0, ((months == null || months.isEmpty()) ? new ArrayList<>() : months));
        this.months = (months == null || months.isEmpty()) ? new ArrayList<>() : months;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull final ViewGroup parent) {
        if(view == null) view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_popup_item, parent, false);

        AppCompatTextView item = view.findViewById(R.id.itemTv);

        final Object[] month = (Object[]) getItem(position);

        item.setText(String.format(Locale.getDefault(), "%s, %s", month[2], month[0]));

        return view;
    }
}
