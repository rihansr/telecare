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
import com.telemedicine.telecare.model.other.ActivityLevel;

import java.util.ArrayList;
import java.util.List;

public class PopupActivityLevelsAdapter extends ArrayAdapter {

    private final List<ActivityLevel> activityLevelItems;

    public PopupActivityLevelsAdapter(@NonNull Context context, List<ActivityLevel> activityLevelItems) {
        super(context, 0, ((activityLevelItems == null || activityLevelItems.isEmpty()) ? new ArrayList<>() : activityLevelItems));
        this.activityLevelItems = (activityLevelItems == null || activityLevelItems.isEmpty()) ? new ArrayList<>() : activityLevelItems;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull final ViewGroup parent) {
        if(view == null) view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_popup_activity_item, parent, false);

        AppCompatTextView level = view.findViewById(R.id.activityLevel_Level_Tv);
        AppCompatTextView details = view.findViewById(R.id.activityLevel_Details_Tv);

        final ActivityLevel activityLevel = (ActivityLevel) getItem(position);

        level.setText(activityLevel.getLevel());
        details.setText(activityLevel.getDetails());

        return view;
    }
}
