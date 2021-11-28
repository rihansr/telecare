package com.telemedicine.telecare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.util.extensions.AppExtensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DaysAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object[]>  days = new ArrayList<>();
    private long            selectedDay = 0;
    private OnDayListener   mOnDayListener;

    public DaysAdapter() {}

    public void setDays(List<Object[]> days, Long selectedDay) {
        this.days = days == null || days.isEmpty() ? new ArrayList<>() : days;
        this.selectedDay = selectedDay == null ? 0 : selectedDay;
        notifyDataSetChanged();
    }

    public void setDays(List<Object[]> days) {
        this.days = days == null || days.isEmpty() ? new ArrayList<>() : days;
        this.selectedDay = 0;
        notifyDataSetChanged();
    }

    public void setSelectedDay(Long selectedDay) {
        this.selectedDay = selectedDay == null ? 0 : selectedDay;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private Object[] day(int position){
        return days.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new DayViewHolder(layoutInflater.inflate(R.layout.sample_day, parent, false));
    }

    private static class DayViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayoutCompat dayHolder;
        private final AppCompatTextView day;
        private final AppCompatTextView weekday;

        private DayViewHolder(View v) {
            super(v);
            dayHolder = v.findViewById(R.id.day_Layout);
            day = v.findViewById(R.id.day_Tv);
            weekday = v.findViewById(R.id.weekday_Tv);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof DayViewHolder){
            DayViewHolder viewHolder = (DayViewHolder) holder;
            boolean isSelectedDay = (long) day(position)[4] == selectedDay;
            updateUI(viewHolder.dayHolder, viewHolder.day, viewHolder.weekday, isSelectedDay);

            viewHolder.day.setText(String.format(Locale.getDefault(), "%s", day(position)[3]));
            viewHolder.weekday.setText(String.format(Locale.getDefault(), "%s", day(position)[1]));

            viewHolder.itemView.setOnClickListener(view -> {
                if(isSelectedDay) return;
                if(mOnDayListener != null) mOnDayListener.onSelect(day(position));
            });
        }
    }

    private void updateUI(LinearLayoutCompat holder, AppCompatTextView day, AppCompatTextView weekday, boolean isMatched){
        holder.setBackground(AppExtensions.getDrawable(isMatched ? R.drawable.shape_curve_accent : R.drawable.shape_curve));
        day.setTextColor(AppExtensions.getColor(isMatched ? R.color.font_Color_Light : R.color.font_Color_Gray));
        weekday.setTextColor(AppExtensions.getColor(isMatched ? R.color.font_Color_Light : R.color.font_Color_Gray));
     }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void setOnDayListener(OnDayListener onDayListener) {
        this.mOnDayListener = onDayListener;
    }

    public interface OnDayListener {
        void onSelect(Object[] day);
    }
}
