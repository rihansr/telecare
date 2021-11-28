package com.telemedicine.telecare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.telemedicine.telecare.R;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.util.extensions.AppExtensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeekdaysAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String>        weekdays = new ArrayList<>();
    private List<String>        selectedWeekdays = new ArrayList<>();
    private OnWeekdaysListener  mOnWeekdaysListener;

    public WeekdaysAdapter() {}

    public void setWeekdays(List<String> weekdays, List<String> selectedWeekdays) {
        this.weekdays = weekdays == null || weekdays.isEmpty() ? new ArrayList<>() : weekdays;
        this.selectedWeekdays = selectedWeekdays == null || selectedWeekdays.isEmpty() ? new ArrayList<>() : selectedWeekdays;
        notifyDataSetChanged();
    }

    public void setSelectedWeekdays(List<String> selectedWeekdays) {
        this.selectedWeekdays = selectedWeekdays == null || selectedWeekdays.isEmpty() ? new ArrayList<>() : selectedWeekdays;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private String weekday(int position){
        return weekdays.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new WeekdayViewHolder(layoutInflater.inflate(R.layout.sample_weekday, parent, false));
    }

    private static class WeekdayViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView weekday;

        private WeekdayViewHolder(View v) {
            super(v);
            weekday = v.findViewById(R.id.weekday_Tv);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof WeekdayViewHolder){
            WeekdayViewHolder viewHolder = (WeekdayViewHolder) holder;

            boolean isSelectedWeekDay = selectedWeekdays.contains(weekday(position));
            updateUI(viewHolder.weekday, isSelectedWeekDay);

            viewHolder.weekday.setText(String.format(Locale.getDefault(), "%s", weekday(position).charAt(0)));

            viewHolder.itemView.setOnClickListener(view -> {
                if(isSelectedWeekDay) selectedWeekdays.remove(weekday(position));
                else selectedWeekdays.add(weekday(position));
                if(mOnWeekdaysListener != null) mOnWeekdaysListener.onSelect(selectedWeekdays);
            });
        }
    }

    private void updateUI(AppCompatTextView weekday, boolean isMatched){
        weekday.setBackgroundTintList(ContextCompat.getColorStateList(AppController.context, isMatched ? R.color.colorAccent : R.color.colorSmokeWhite));
        weekday.setTextColor(AppExtensions.getColor(isMatched ? R.color.font_Color_Light : R.color.font_Color_Gray));
     }

    @Override
    public int getItemCount() {
        return weekdays.size();
    }

    public void setOnWeekdaysListener(OnWeekdaysListener onWeekdaysListener) {
        this.mOnWeekdaysListener = onWeekdaysListener;
    }

    public interface OnWeekdaysListener {
        void onSelect(List<String> weekdays);
    }
}
