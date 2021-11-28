package com.telemedicine.telecare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.model.appointment.AppointmentTiming;
import com.telemedicine.telecare.model.appointment.Timing;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.enums.Role;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppointmentTimingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AppointmentTiming>     appointmentTimings = new ArrayList<>();
    private OnAppointmentTimingListener mOnAppointmentTimingListener;

    public AppointmentTimingsAdapter() {}

    public void setAppointmentTimings(List<AppointmentTiming> appointmentTimings) {
        this.appointmentTimings = appointmentTimings == null || appointmentTimings.isEmpty() ? new ArrayList<>() : appointmentTimings;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private AppointmentTiming appointmentTiming(int position){
        return appointmentTimings.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new AppointmentTimingViewHolder(layoutInflater.inflate(R.layout.sample_appointment_timing, parent, false));
    }

    private static class AppointmentTimingViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView     chamber;
        private final LinearLayoutCompat    assistantLayout;
        private final AppCompatTextView     assistantDetails;
        private final AppCompatTextView     timings;
        private final AppCompatImageButton  removeButton;
        private final AppCompatTextView     setButton;

        private AppointmentTimingViewHolder(View v) {
            super(v);
            chamber = v.findViewById(R.id.appointment_ChamberAddress_Tv);
            assistantLayout = v.findViewById(R.id.appointment_Assistant_Layout);
            assistantDetails = v.findViewById(R.id.appointment_AssistantInfo_Tv);
            timings = v.findViewById(R.id.appointment_Timings_Tv);
            removeButton = v.findViewById(R.id.appointment_Remove_Button);
            setButton = v.findViewById(R.id.appointment_Set_Button);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof AppointmentTimingViewHolder){
            AppointmentTimingViewHolder viewHolder = (AppointmentTimingViewHolder) holder;

            viewHolder.chamber.setText(appointmentTiming(position).getChamberAddress());

            String assistantDetails = appointmentTiming(position).getAssistantName() == null
                    ? (appointmentTiming(position).getAssistantPhone() == null
                        ? null
                        : appointmentTiming(position).getAssistantPhone())
                    : (appointmentTiming(position).getAssistantPhone() == null
                        ? appointmentTiming(position).getAssistantName()
                        : String.format(Locale.getDefault(), "%s (%s)", appointmentTiming(position).getAssistantName(), appointmentTiming(position).getAssistantPhone()));

            viewHolder.assistantLayout.setVisibility(assistantDetails != null ? View.VISIBLE : View.GONE);
            viewHolder.assistantDetails.setText(assistantDetails);

            viewHolder.timings.setText(getTimings(appointmentTiming(position).getTimings()));

            viewHolder.setButton.setVisibility(LocalStorage.USER.getRole().equals(Role.PATIENT.getId()) ? View.VISIBLE : View.GONE);
            viewHolder.setButton.setOnClickListener(view -> {
                if(mOnAppointmentTimingListener != null) mOnAppointmentTimingListener.onSelect(appointmentTiming(position));
            });

            viewHolder.removeButton.setVisibility(LocalStorage.USER.getRole().equals(Role.DOCTOR.getId()) ? View.VISIBLE : View.GONE);
            viewHolder.removeButton.setOnClickListener(view -> {
                if(mOnAppointmentTimingListener != null) mOnAppointmentTimingListener.onRemove(appointmentTiming(position));
            });
        }
    }

    public static String getTimings(List<Timing> timings){
        if(timings == null || timings.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < timings.size(); i++) {
            sb.append(AppExtensions.getTiming(timings.get(i)).get("timing"));
            if (i != (timings.size() - 1)) sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return appointmentTimings.size();
    }

    public void setOnAppointmentTimingListener(OnAppointmentTimingListener onAppointmentTimingListener) {
        this.mOnAppointmentTimingListener = onAppointmentTimingListener;
    }

    public interface OnAppointmentTimingListener {
        void onSelect(AppointmentTiming appointmentTiming);
        void onRemove(AppointmentTiming appointmentTiming);
    }
}
