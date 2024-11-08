package com.telemedicine.telecare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.fragment.ChatMessagesFragment;
import com.telemedicine.telecare.fragment.PhotoViewFragment;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.model.chat.Group;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.enums.Role;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.telemedicine.telecare.util.extensions.DateExtensions;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatGroupsAdapter extends RecyclerView.Adapter<ChatGroupsAdapter.ViewHolder> {

    private List<Group> groups = new ArrayList<>();

    public ChatGroupsAdapter() {}

    public void setChatGroups(List<Group> groups) {
        this.groups = groups == null || groups.isEmpty() ? new ArrayList<>() : groups;
        notifyDataSetChanged();
    }

    private Group group(int position){
        return groups.get(position);
    }

    @NonNull
    @Override
    public ChatGroupsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.sample_chat_group_item, parent, false);
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatImageView  user_Photo;
        private final AppCompatImageView  user_ActiveIcon;
        private final AppCompatTextView   user_Name;
        private final AppCompatImageView  newMessage_Icon;
        private final AppCompatTextView   user_NewMessage;
        private final AppCompatTextView   message_Time;

        private ViewHolder(View v) {
            super(v);
            user_Photo = v.findViewById(R.id.chat_UserPhoto_Iv);
            user_ActiveIcon = v.findViewById(R.id.chat_UserActive_Icon);
            user_Name = v.findViewById(R.id.chat_UserName_Tv);
            newMessage_Icon = v.findViewById(R.id.chat_NewMessage_Iv);
            user_NewMessage = v.findViewById(R.id.chat_Message_Tv);
            message_Time = v.findViewById(R.id.chat_Time_Tv);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatGroupsAdapter.ViewHolder holder, final int pos) {
        group(pos).getUsers().remove(LocalStorage.USER.getId());
        String userId = group(pos).getUsers().get(0);

        FirebaseFirestore.getInstance().collection(FirebaseHelper.USERS_TABLE)
                .whereEqualTo(User.ID, userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        User user = snapshot.toObject(User.class);

                        boolean isVerified = user.isProfileVerified() || LocalStorage.USER.getRole().equals(Role.ADMIN.getId());

                        holder.user_Name.setText(isVerified ? user.getName() : AppExtensions.getString(R.string.unknown));
                        AppExtensions.loadPhoto(holder.user_Photo, isVerified ? user.getPhoto() : null, R.dimen.icon_Size_X_Large, R.drawable.ic_avatar);
                        holder.user_ActiveIcon.setVisibility(user.getActive().getStatus() && isVerified ? View.VISIBLE : View.GONE);
                        holder.itemView.setOnClickListener(view -> ChatMessagesFragment.show(user, group(pos), null));

                        holder.user_Photo.setOnLongClickListener(view -> {
                            if(isVerified) PhotoViewFragment.show(user.getPhoto());
                            return false;
                        });
                        break;
                    }
                }
        );

        if(group(pos).getLastMessage().getFiles() != null && !group(pos).getLastMessage().getFiles().isEmpty()){
            holder.user_NewMessage.setText(AppExtensions.getString(R.string.sentAnAttachment));
        }
        else {
            holder.user_NewMessage.setText(group(pos).getLastMessage().getMessage());
        }

        holder.message_Time.setText(String.format(Locale.getDefault(), "%s %s",
                AppExtensions.getString(R.string.bullet),
                new DateExtensions(group(pos).getLastMessage().getSentAt().getTime()).defaultTimeFormat()));

        if(group(pos).getLastMessageSeenBy().containsKey(LocalStorage.USER.getId())){
            holder.newMessage_Icon.setVisibility(View.GONE);
            holder.user_NewMessage.setTextColor(AppExtensions.getColor(R.color.font_Color_Gray));
            holder.message_Time.setTextColor(AppExtensions.getColor(R.color.font_Color_Gray));
        }
        else {
            holder.newMessage_Icon.setVisibility(View.VISIBLE);
            holder.user_NewMessage.setTextColor(AppExtensions.getColor(R.color.font_Color_Dark));
            holder.message_Time.setTextColor(AppExtensions.getColor(R.color.font_Color_Dark));
        }
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }
}
