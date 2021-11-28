package com.telemedicine.telecare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.model.appointment.Timing;
import com.telemedicine.telecare.util.extensions.AppExtensions;

import java.util.ArrayList;
import java.util.List;

public class TimingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Timing>        timings = new ArrayList<>();
    private OnTimingListener    mOnTimingListener;

    public TimingAdapter() {}

    public void setTimings(List<Timing> timings) {
        this.timings = timings == null || timings.isEmpty() ? new ArrayList<>() : timings;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private Timing timing(int position){
        return timings.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new TimingViewHolder(layoutInflater.inflate(R.layout.sample_timing, parent, false));
    }

    private static class TimingViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView     weekday;
        private final AppCompatTextView     time;
        private final AppCompatImageButton  editButton;
        private final AppCompatImageButton  removeButton;

        private TimingViewHolder(View v) {
            super(v);
            weekday = v.findViewById(R.id.timing_Weekday_Tv);
            time = v.findViewById(R.id.timing_Time_Tv);
            editButton = v.findViewById(R.id.timing_Edit_Button);
            removeButton = v.findViewById(R.id.timing_Remove_Button);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof TimingViewHolder){
            TimingViewHolder viewHolder = (TimingViewHolder) holder;

            viewHolder.weekday.setText(AppExtensions.getTiming(timing(position)).get("weekday"));
            viewHolder.time.setText(AppExtensions.getTiming(timing(position)).get("time"));

            viewHolder.editButton.setOnClickListener(view -> {
                if(mOnTimingListener != null) mOnTimingListener.onEdit(position, timing(position));
            });

            viewHolder.removeButton.setOnClickListener(view -> {
                if(mOnTimingListener != null) mOnTimingListener.onRemove(position, timing(position));
            });
        }
    }

    @Override
    public int getItemCount() {
        return timings.size();
    }

    public void setOnTimingListener(OnTimingListener onTimingListener) {
        this.mOnTimingListener = onTimingListener;
    }

    public interface OnTimingListener {
        void onEdit(int position, Timing foodAmount);
        void onRemove(int position, Timing foodAmount);
    }
}
