package com.telemedicine.telecare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.telemedicine.telecare.util.extensions.DateExtensions;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object[]>     times = new ArrayList<>();
    private long               selectedTime = 0;
    private List<Date[]>       existingSlots = new ArrayList();
    private OnTimeSlotListener mOnTimeSlotListener;

    public TimesAdapter() {}

    public void setItems(List<Object[]> items, List<Date[]> slots) {
        this.times = items == null || items.isEmpty() ? new ArrayList<>() : items;
        this.existingSlots = slots == null || slots.isEmpty() ? new ArrayList<>() : slots;
        this.selectedTime = 0;
        notifyDataSetChanged();
    }

    public void setTimes(List<Object[]> times) {
        this.times = times == null || times.isEmpty() ? new ArrayList<>() : times;
        this.selectedTime = 0;
        notifyDataSetChanged();
    }

    public void setSelectedTime(Long selectedTime){
        this.selectedTime = selectedTime == null ? 0 : selectedTime;
        notifyDataSetChanged();
    }

    public void setExistingSlots(List<Date[]> slots){
        this.existingSlots = slots == null || slots.isEmpty() ? new ArrayList<>() : slots;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private Object[] timeSlot(int position){
        return times.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new TimeViewHolder(layoutInflater.inflate(R.layout.sample_time, parent, false));
    }

    private static class TimeViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView time;

        private TimeViewHolder(View v) {
            super(v);
            time = v.findViewById(R.id.itemTv);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof TimeViewHolder) {
            TimeViewHolder viewHolder = (TimeViewHolder) holder;

            boolean dateHasPassed = ((Date) timeSlot(position)[2]).getTime() <= System.currentTimeMillis();
            boolean isSlotBooked = isSlotBooked(timeSlot(position));
            boolean isSlotSelected = ((Date) timeSlot(position)[0]).getTime() == selectedTime;

            updateUI(viewHolder.time, dateHasPassed, isSlotBooked, isSlotSelected);
            viewHolder.time.setText(String.format(Locale.getDefault(), "%s", timeSlot(position)[1]));

            holder.itemView.setOnClickListener(view -> {
                if(dateHasPassed || isSlotSelected) return;
                if(isSlotBooked) {
                    new CustomSnackBar(holder.itemView,
                            String.format(Locale.getDefault(), "%s - %s %s", timeSlot(position)[1], timeSlot(position)[3], AppExtensions.getString(R.string.alreadyBooked)),
                            R.string.okay,
                            CustomSnackBar.Duration.SHORT).show();
                    return;
                }
                if(mOnTimeSlotListener != null) mOnTimeSlotListener.onSelect(timeSlot(position));
            });
        }
    }

    private boolean isSlotBooked(Object[] timeSlot){
        if (!existingSlots.isEmpty()) {
            for (Date[] slot : existingSlots) {
                if (new DateExtensions().isDateOverlap(slot[0], slot[1], (Date) timeSlot[0])
                        || new DateExtensions().isDateOverlap(slot[0], slot[1], new Date(((Date) timeSlot[2]).getTime() - 1)))
                    return true;
            }
        }
        return false;
    }

    private void updateUI(AppCompatTextView view, boolean dateHasPassed, boolean isSlotBooked, boolean isSlotSelected){
        view.setBackground(AppExtensions.getDrawable(dateHasPassed ? R.drawable.shape_curve : isSlotBooked
                ? R.drawable.shape_curve_red
                : isSlotSelected ? R.drawable.shape_curve_accent : R.drawable.shape_curve)
        );

        view.setTextColor(AppExtensions.getColor(dateHasPassed ? R.color.colorSmokeWhite : isSlotBooked
                ? R.color.font_Color_Red
                : isSlotSelected ? R.color.font_Color_Light : R.color.font_Color_Gray));
     }

    @Override
    public int getItemCount() {
        return times.size();
    }

    public void setOnItemListener(OnTimeSlotListener onTimeSlotListener) {
        this.mOnTimeSlotListener = onTimeSlotListener;
    }

    public interface OnTimeSlotListener {
        void onSelect(Object[] item);
    }
}
