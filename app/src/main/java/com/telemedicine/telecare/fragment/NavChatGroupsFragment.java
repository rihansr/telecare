package com.telemedicine.telecare.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.adapter.ChatGroupsAdapter;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.model.chat.Group;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.extensions.AppExtensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NavChatGroupsFragment extends Fragment {

    private View                 rootView;

    private RecyclerView         rcv_Chats;
    private ChatGroupsAdapter    chatGroupsAdapter;

    private LinearLayoutCompat   empty_Layout;
    private LottieAnimationView  empty_Icon;
    private AppCompatTextView    empty_Title;
    private AppCompatTextView    empty_Subtitle;

    private ListenerRegistration chatGroupsListenerRegistration;
    private LoadingFragment      loading;

    public NavChatGroupsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_nav_chats, container, false);
        initId();
        init();
        return rootView;
    }

    private void initId() {
        rcv_Chats = rootView.findViewById(R.id.chat_Chats_Rcv);
        empty_Layout = rootView.findViewById(R.id.chats_Empty_Layout);
        empty_Icon = rootView.findViewById(R.id.empty_Icon_Animation);
        empty_Title = rootView.findViewById(R.id.empty_Title_Tv);
        empty_Subtitle = rootView.findViewById(R.id.empty_Subtitle_Tv);
    }

    private void init() {
        empty_Icon.setAnimation("lottie/blank_chats.json");
        empty_Title.setText(AppExtensions.getString(R.string.emptyChatsTitle));
        empty_Subtitle.setText(AppExtensions.getString(R.string.emptyChatsSubtitle));

        setAdapter();

        getChatGroups();
    }

    private void setAdapter(){
        chatGroupsAdapter = new ChatGroupsAdapter();
        rcv_Chats.setAdapter(chatGroupsAdapter);
    }

    private void getChatGroups() {
        loading = LoadingFragment.show();
        EventListener<QuerySnapshot> chatGroupsEventListener = (requestSnapshot, error) -> {
            AppExtensions.dismissLoading(loading);

            /** Error & Null Data Checking **/
            if (error != null) {
                chatGroupsAdapter.setChatGroups(new ArrayList<>());
                Log.e(Constants.TAG, "Chat Group Error, Reason: " + error.getCode(), error);
                if(Objects.requireNonNull(error.getMessage()).contains("PERMISSION_DENIED")){
                    empty_Icon.setAnimation("lottie/not_verified.json");
                    empty_Layout.setVisibility(View.VISIBLE);
                }
                return;
            }
            else if (requestSnapshot == null || requestSnapshot.isEmpty()) {
                Log.e(Constants.TAG, "No Chat Groups");
                chatGroupsAdapter.setChatGroups(new ArrayList<>());
                empty_Icon.setAnimation("lottie/blank_chats.json");
                empty_Layout.setVisibility(View.VISIBLE);
                return;
            }

            /** Get All Chat Groups **/
            List<Group> chatGroups = new ArrayList<>();
            for (QueryDocumentSnapshot snapshot : requestSnapshot) {
                Group group = snapshot.toObject(Group.class);
                chatGroups.add(group);
            }

            chatGroupsAdapter.setChatGroups(chatGroups);

            empty_Icon.setAnimation("lottie/blank_chats.json");
            empty_Layout.setVisibility(chatGroups.size() == 0 ? View.VISIBLE : View.GONE);
        };

        chatGroupsListenerRegistration = FirebaseFirestore.getInstance()
                .collection(FirebaseHelper.CHAT_GROUPS_TABLE)
                .whereArrayContains(Group.USERS, LocalStorage.USER.getId())
                .addSnapshotListener(chatGroupsEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(chatGroupsListenerRegistration != null) chatGroupsListenerRegistration.remove();
    }
}