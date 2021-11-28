package com.telemedicine.telecare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.fragment.PhotoViewFragment;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.model.appointment.Appointment;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.enums.Role;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.telemedicine.telecare.util.extensions.DateExtensions;
import com.telemedicine.telecare.wiget.CircleImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppointmentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Appointment>       appointments = new ArrayList<>();
    private OnAppointmentListener   mOnAppointmentListener;

    public AppointmentsAdapter() {}

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments == null || appointments.isEmpty() ? new ArrayList<>() : appointments;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private Appointment appointment(int position){
        return appointments.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new AppointmentViewHolder(layoutInflater.inflate(R.layout.sample_appointment, parent, false));
    }

    private static class AppointmentViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView     name;
        private final AppCompatTextView     info;
        private final CircleImageView       photo;
        private final AppCompatTextView     chamber;
        private final AppCompatTextView     timing;
        private final AppCompatTextView     report;
        private final AppCompatTextView     consultation;
        private final AppCompatTextView     notes;
        private final AppCompatImageButton  remove;

        private AppointmentViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.appointment_UserName_Tv);
            info = v.findViewById(R.id.appointment_UserInfo_Tv);
            photo = v.findViewById(R.id.appointment_UserPhoto_Iv);
            chamber = v.findViewById(R.id.appointment_ChamberInfo_Tv);
            timing = v.findViewById(R.id.appointment_Timing_Tv);
            consultation = v.findViewById(R.id.appointment_Consultation_Tv);
            report = v.findViewById(R.id.appointment_Report_Tv);
            notes = v.findViewById(R.id.appointment_Notes_Tv);
            remove = v.findViewById(R.id.appointment_Remove_Button);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof AppointmentViewHolder){
            AppointmentViewHolder viewHolder = (AppointmentViewHolder) holder;

            boolean isADoctor = LocalStorage.USER.getRole().equals(Role.DOCTOR.getId());
            String userId = isADoctor ? appointment(position).getAppointedBy() : appointment(position).getAppointedTo();

            FirebaseFirestore.getInstance().collection(FirebaseHelper.USERS_TABLE)
                    .whereEqualTo(User.ID, userId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                    User user = snapshot.toObject(User.class);

                                    boolean isVerified = user.isProfileVerified() || LocalStorage.USER.getRole().equals(Role.ADMIN.getId());

                                    String name = isADoctor ? (appointment(position).isPatient() ? user.getName() : appointment(position).getPatientName()) : user.getName();
                                    viewHolder.name.setText(isVerified ? name : AppExtensions.getString(R.string.unknown));

                                    String photo = isADoctor ? (appointment(position).isPatient() ? user.getPhoto() : null) : user.getPhoto();
                                    AppExtensions.loadPhoto(viewHolder.photo, isVerified ? photo : null, R.dimen.icon_Size_X_Large, R.drawable.ic_avatar);

                                    viewHolder.photo.setOnLongClickListener(view -> {
                                        if(isVerified) PhotoViewFragment.show(user.getPhoto());
                                        return false;
                                    });

                                    String gender = isADoctor ? (appointment(position).isPatient() ? user.getGender() : appointment(position).getPatientGender()) : user.getGender();

                                    String age = user.getAge() == null
                                            ? ""
                                            : " " + (AppExtensions.getString(R.string.bullet) + " " +  (appointment(position).isPatient() ? user.getAge() : appointment(position).getPatientAge())) + " " +  AppExtensions.getString(R.string.yrs);

                                    String specialty = user.getSpecialty() == null ? "" : " " + (AppExtensions.getString(R.string.bullet) + " " +  user.getSpecialty().getTitle());

                                    viewHolder.info.setText(String.format(Locale.getDefault(),"%s%s",
                                            gender,
                                            LocalStorage.USER.getRole().equals(Role.DOCTOR.getId()) ? age : specialty)
                                    );

                                    holder.itemView.setOnClickListener(v -> {
                                        if(mOnAppointmentListener != null) mOnAppointmentListener.onSelect(appointment(position), user);
                                    });

                                    break;
                                }
                            }
                    );

            viewHolder.chamber.setText(appointment(position).getChamber());

            viewHolder.timing.setText(String.format(Locale.getDefault(),
                    "%s (%s - %s)",
                    new DateExtensions(appointment(position).getStartAt()).appointmentDateFormat(),
                    new DateExtensions(appointment(position).getStartAt()).defaultTimeFormat(),
                    new DateExtensions(appointment(position).getEndAt()).defaultTimeFormat())
            );

            viewHolder.notes.setVisibility(appointment(position).getNotes() != null ? View.VISIBLE : View.GONE);
            viewHolder.notes.setText(appointment(position).getNotes());

            viewHolder.consultation.setVisibility(appointment(position).isConsultation() ? View.VISIBLE : View.GONE);
            viewHolder.report.setVisibility(appointment(position).isReport() ? View.VISIBLE : View.GONE);

            viewHolder.remove.setOnClickListener(v -> {
                if(mOnAppointmentListener != null) mOnAppointmentListener.onRemove(appointment(position));
            });
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void setOnAppointmentListener(OnAppointmentListener mOnAppointmentListener) {
        this.mOnAppointmentListener = mOnAppointmentListener;
    }

    public interface OnAppointmentListener {
        void onSelect(Appointment appointment, User user);
        void onRemove(Appointment appointment);
    }
}
