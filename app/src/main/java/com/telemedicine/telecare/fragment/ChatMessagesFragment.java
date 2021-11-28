package com.telemedicine.telecare.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.telemedicine.telecare.BuildConfig;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.adapter.ChatMessagesAdapter;
import com.telemedicine.telecare.base.AppBaseActivity;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.helper.NotificationManager;
import com.telemedicine.telecare.helper.PermissionManager;
import com.telemedicine.telecare.model.chat.Group;
import com.telemedicine.telecare.model.chat.Message;
import com.telemedicine.telecare.model.other.File;
import com.telemedicine.telecare.model.other.Request;
import com.telemedicine.telecare.model.other.Status;
import com.telemedicine.telecare.model.user.User;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.enums.Photo;
import com.telemedicine.telecare.util.enums.Role;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.telemedicine.telecare.util.extensions.DateExtensions;
import com.telemedicine.telecare.wiget.CircleImageView;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import static android.app.Activity.RESULT_OK;

public class ChatMessagesFragment extends DialogFragment implements AppBaseActivity.OnUserRequestListener {

    private static final String     TAG = ChatMessagesFragment.class.getSimpleName();
    private Context                 context;
    private Activity                activity;

    /**
     * Toolbar
     **/
    private AppCompatImageView      back_Button;
    private CircleImageView         user_Photo;
    private AppCompatTextView       user_Name;
    private AppCompatTextView       user_ActiveStatus;
    private AppCompatImageView      user_ActiveIcon;
    private AppCompatImageView      call_Button;

    /**
     * Messages
     **/
    private RecyclerView            rcv_Messages;
    private ChatMessagesAdapter     chatMessagesAdapter;

    /**
     * User Actions
     **/
    private AppCompatTextView       cantReply_Tv;
    private AppCompatEditText       message_Input;
    private AppCompatImageButton    expand_Button;
    private AppCompatImageButton    camera_Button;
    private AppCompatImageButton    gallery_Button;
    private AppCompatImageButton    send_Button;

    private Group                   chatGroup = null;
    private User                    messageReceiver;
    private ListenerRegistration    messagesListenerRegistration;
    private ListenerRegistration    userInfoListenerRegistration;
    private ListenerRegistration    chatGroupsListenerRegistration;
    private FirebaseHelper          firebaseHelper;
    private LoadingFragment         loading;
    private ProgressDialog          progressDialog;

    public static ChatMessagesFragment show(User user, Group group, List<Message> messages){
        ChatMessagesFragment fragment = new ChatMessagesFragment();
        if(user != null){
            Bundle args = new Bundle();
            args.putSerializable(Constants.USER_BUNDLE_KEY, user);
            args.putSerializable(Constants.CHAT_GROUP_BUNDLE_KEY, group);
            args.putSerializable(Constants.MESSAGES_BUNDLE_KEY, (Serializable) messages);
            fragment.setArguments(args);
        }
        fragment.show(((AppCompatActivity) AppController.getActivity()).getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_TopLight_FadeAnimation);
        setRetainInstance(true);
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_chat_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), false);
        super.onViewCreated(view, savedInstanceState);

        initId(view);

        init();
    }

    private void initId(View view) {
        back_Button = view.findViewById(R.id.chat_Back_Button);
        user_Photo = view.findViewById(R.id.chat_UserPhoto_Iv);
        user_ActiveIcon = view.findViewById(R.id.chat_UserActive_Icon);
        user_Name = view.findViewById(R.id.chat_UserName_Tv);
        user_ActiveStatus= view.findViewById(R.id.chat_ActiveStatus_Tv);
        call_Button = view.findViewById(R.id.chat_AudioCall_Button);

        rcv_Messages = view.findViewById(R.id.chat_Messages_Rcv);
        message_Input = view.findViewById(R.id.chat_Message_Input);

        cantReply_Tv = view.findViewById(R.id.chat_CantReply_Tv);
        expand_Button = view.findViewById(R.id.chat_Expand_Button);
        camera_Button = view.findViewById(R.id.chat_Camera_Button);
        gallery_Button = view.findViewById(R.id.chat_Gallery_Button);
        send_Button = view.findViewById(R.id.chat_Send_Button);

        firebaseHelper = new FirebaseHelper();
        progressDialog = new ProgressDialog(activity, R.style.ProgressDialog);
    }

    private void init(){
        if (getArguments() == null) { dismiss(); return; }
        messageReceiver = (User) getArguments().getSerializable(Constants.USER_BUNDLE_KEY);
        getArguments().remove(Constants.USER_BUNDLE_KEY);
        if (messageReceiver == null) { dismiss(); return; }

        ((AppBaseActivity) activity).setOnUserRequestListener(this);

        setAdapter();

        checkUserHasAccess();
        
        chatGroup = (Group) getArguments().getSerializable(Constants.CHAT_GROUP_BUNDLE_KEY);
        getArguments().remove(Constants.CHAT_GROUP_BUNDLE_KEY);
        getChatGroup();

        getReceiverInfo();
        
        getChatMessages();

        List<Message> messages = (List<Message>) getArguments().getSerializable(Constants.MESSAGES_BUNDLE_KEY);
        chatMessagesAdapter.setMessages(messages);
        getArguments().remove(Constants.MESSAGES_BUNDLE_KEY);

        back_Button.setOnClickListener(view -> dismiss());

        camera_Button.setOnClickListener(view -> captureByCamera());

        gallery_Button.setOnClickListener(view -> pickFromGallery(R.string.select_Photo));

        message_Input.setOnFocusChangeListener((view, hasFocus) -> {
            new Handler().postDelayed(() -> {
                for (AppCompatImageButton button : new AppCompatImageButton[]{camera_Button, gallery_Button}){
                    button.setVisibility(hasFocus ? View.GONE : View.VISIBLE);
                    button.setEnabled(!hasFocus);
                }
            }, hasFocus ? 0 : 100);

            expand_Button.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        });

        expand_Button.setOnClickListener(view -> message_Input.clearFocus());

        send_Button.setOnClickListener(view -> {
            String getMessage = Objects.requireNonNull(message_Input.getText()).toString().trim();
            if(!getMessage.isEmpty()) sendMessage(getMessage, null);
        });
    }

    private void setAdapter() {
        chatMessagesAdapter = new ChatMessagesAdapter(context);
        rcv_Messages.setAdapter(chatMessagesAdapter);
        chatMessagesAdapter.setOnMessageListener(bottomPosition -> rcv_Messages.smoothScrollToPosition(bottomPosition));
    }

    private void checkUserHasAccess(){
        boolean hasAccess = hasUserAccess();
        boolean hasChatAccess = hasAccess && messageReceiver.getChatOption();
        boolean hasCallAccess = hasAccess && messageReceiver.getCallOption();

        cantReply_Tv.setVisibility(hasChatAccess ? View.GONE : View.VISIBLE);

        camera_Button.setEnabled(hasChatAccess);
        gallery_Button.setEnabled(hasChatAccess);
        send_Button.setEnabled(hasChatAccess);

        call_Button.setOnClickListener(view -> {
            if(hasCallAccess) { AppExtensions.call(messageReceiver.getPhone(), messageReceiver.getPhone()); return; }
            String message = AppExtensions.getString(R.string.youCantCall)
                    + " "
                    + (LocalStorage.USER.getRole().equals(Role.PATIENT.getId()) ? Role.DOCTOR.getTitle().toLowerCase() : Role.PATIENT.getTitle().toLowerCase())
                    + "!";
            new CustomSnackBar(AppExtensions.getRootView(getDialog()), message, R.string.okay, CustomSnackBar.Duration.SHORT).show();
        });

        setReceiverInfo(hasAccess);
    }

    private boolean hasUserAccess(){
        /** Admin has access to call/chat **/
        if(LocalStorage.USER.getRole().equals(Role.ADMIN.getId()) && LocalStorage.USER.isProfileVerified()) return true;

        /** Check Sender/Receiver verified or not **/
        if(!messageReceiver.isProfileVerified() || !LocalStorage.USER.isProfileVerified()) return false;

        /** User has access to call/chat with admin **/
        if(messageReceiver.getRole().equals(Role.ADMIN.getId())) return true;

        /** Check patients has access to call/chat **/
        for (Request request : LocalStorage.USER.getRole().equals(Role.PATIENT.getId()) ? LocalStorage.sentRequests : LocalStorage.userRequests) {
            if (request.getRejected().getStatus()) continue;
            if (!request.getAccepted().getStatus()) continue;
            if (!(request.getRequestedTo().equals(messageReceiver.getId()) || request.getRequestedBy().equals(messageReceiver.getId()))) continue;
            if (request.getAccepted().getStatus()) return true;
        }

        /** Access denied **/
        return false;
    }

    private void getChatGroup() {
        if(chatGroup != null) { getChatMessages();  return; }
        loading = LoadingFragment.show();
        EventListener<QuerySnapshot> chatGroupsEventListener = (requestSnapshot, error) -> {
            AppExtensions.dismissLoading(loading);

            /** Error & Null Data Checking **/
            if (error != null) {
                Log.e(Constants.TAG, "Chat Group Error, Reason: " + error.getMessage(), error);
                return;
            }
            else if (requestSnapshot == null || requestSnapshot.isEmpty()) {
                chatGroup = new Group();
                Log.e(Constants.TAG, "No Chat Groups");
                return;
            }

            /** Get Chat Group **/
            for (QueryDocumentSnapshot snapshot : requestSnapshot) {
                Group group = snapshot.toObject(Group.class);
                if (group.getUsers().contains(messageReceiver.getId())) {
                    chatGroup = group;
                    getChatMessages();
                    chatGroupsListenerRegistration.remove();
                    break;
                }
            }

            if(chatGroup == null) chatGroup = new Group();
        };

        chatGroupsListenerRegistration = FirebaseFirestore.getInstance()
                .collection(FirebaseHelper.CHAT_GROUPS_TABLE)
                .whereArrayContains(Group.USERS, LocalStorage.USER.getId())
                .addSnapshotListener(chatGroupsEventListener);
    }

    private void getChatMessages() {
        if(chatGroup == null || chatGroup.getId() == null) return;
        EventListener<QuerySnapshot> messagesEventListener = (requestSnapshot, error) -> {

            /** Error & Null Data Checking **/
            if (error != null) {
                Log.e(Constants.TAG, "Message Error, Reason: " + error.getMessage(), error);
                return;
            }
            else if (requestSnapshot == null || requestSnapshot.isEmpty()) {
                Log.e(Constants.TAG, "No Messages");
                chatMessagesAdapter.setMessages(new ArrayList<>());
                return;
            }

            /** Get All Messages **/
            List<Message> messages = new ArrayList<>();
            for (QueryDocumentSnapshot snapshot : requestSnapshot) {
                Message message = snapshot.toObject(Message.class);
                messages.add(message);
            }

            chatMessagesAdapter.setMessages(messages);
        };

        messagesListenerRegistration = FirebaseFirestore.getInstance()
                .collection(FirebaseHelper.CHAT_MESSAGES_TABLE)
                .document(chatGroup.getId())
                .collection(FirebaseHelper.MESSAGES_COLLECTION)
                .orderBy(Message.SENT_AT, Query.Direction.ASCENDING)
                .addSnapshotListener(messagesEventListener);
    }

    private void getReceiverInfo() {
        EventListener<DocumentSnapshot> userInfoEventListener = (userSnapshot, error) -> {
            /** Error & Null Data Checking **/
            if (error != null) {
                Log.e(Constants.TAG, "Receiver Info Error, Reason: " + error.getMessage(), error);
                return;
            }
            else if (userSnapshot == null || !userSnapshot.exists()) {
                Log.e(Constants.TAG, "No Users");
                messageReceiver.setProfileVerified(false);
                checkUserHasAccess();
                return;
            }

            /** Get Receiver Info **/
            messageReceiver = userSnapshot.toObject(User.class);
            checkUserHasAccess();
        };

        userInfoListenerRegistration = FirebaseFirestore.getInstance()
                .collection(FirebaseHelper.USERS_TABLE)
                .document(messageReceiver.getId())
                .addSnapshotListener(userInfoEventListener);
    }

    private void setReceiverInfo(Boolean hasAccess){
        boolean hasUserAccess = hasAccess != null ? hasAccess : hasUserAccess();
        AppExtensions.loadPhoto(user_Photo, hasUserAccess ? messageReceiver.getPhoto() : null, R.dimen.icon_Size_Medium, R.drawable.ic_avatar);
        user_Photo.setOnClickListener(view -> {
            if(!hasUserAccess) return;
            if (LocalStorage.USER.getRole().equals(Role.DOCTOR.getId())) PatientProfileFragment.show(messageReceiver);
            else DoctorProfileFragment.show(messageReceiver).setOnProfileUpdateListener(isUpdated -> checkUserHasAccess());
        });
        user_ActiveIcon.setVisibility(messageReceiver.getActive().getStatus() && hasUserAccess ? View.VISIBLE : View.GONE);
        user_Name.setText(messageReceiver.getName());
        user_ActiveStatus.setVisibility(hasUserAccess ? View.VISIBLE : View.GONE);
        user_ActiveStatus.setText(messageReceiver.getActive().getStatus() ? "Active Now" : new DateExtensions(messageReceiver.getActive().getDate().getTime()).getTimeAgo());
    }

    private void sendMessage(String senderMessage, List<File> files) {
        message_Input.setText(null);
        if(!isDone()) return;

        String chatUID = chatGroup.getId() == null ? UUID.randomUUID().toString() : chatGroup.getId();
        List<String> userIds = new ArrayList<>();
        userIds.add(LocalStorage.USER.getId());
        userIds.add(messageReceiver.getId());

        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setSenderRole(Constants.roleMode.getId());
        message.setSenderId(LocalStorage.USER.getId());
        message.setMessage(senderMessage);
        message.setFiles(files);
        message.setDeletedBy(null);
        message.setSentAt(new Date());

        Group group = new Group();
        group.setId(chatUID);
        group.setUsers(userIds);
        group.setLastMessage(message);

        HashMap<String, Status> seenBy = new HashMap<>();
        seenBy.put(LocalStorage.USER.getId(), new Status(true, new Date()));
        group.setLastMessageSeenBy(seenBy);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        DocumentReference groupReference = db.collection(FirebaseHelper.CHAT_GROUPS_TABLE).document(chatUID);

        if(chatGroup.getId() == null) batch.set(groupReference, group);
        else batch.update(groupReference, Group.LAST_MESSAGE, message, Group.SEEN_BY, seenBy);

        DocumentReference messagesReference = db.collection(FirebaseHelper.CHAT_MESSAGES_TABLE).document(chatUID)
                .collection(FirebaseHelper.MESSAGES_COLLECTION)
                .document(message.getId());

        batch.set(messagesReference, message);

        batch.commit().addOnCompleteListener(task -> {
            progressDialog.dismiss();

            if(task.isSuccessful()){
                HashMap<String, String> data = new HashMap<>();
                data.put("userLoggedIn", "1");
                data.put("allowOnlyInBackground", "1");
                data.put("chatGroup", new Gson().toJson(group));
                data.put("messageSender", new Gson().toJson(LocalStorage.USER));
                data.put("messageReceiverId", messageReceiver.getId());
                data.put("receiverRoleId", messageReceiver.getRole());

                new NotificationManager(
                        messageReceiver.getToken(),
                        LocalStorage.USER.getName(),
                        (message.getFiles() != null ? AppExtensions.getString(R.string.sentAnAttachment) : senderMessage),
                        new Gson().toJson(data)
                ).send();
            }
            else {
                AppExtensions.toast(R.string.sendingMessageFailed);
            }
        });
    }

    private boolean isDone(){
        if(!Constants.IS_INTERNET_CONNECTED){
            AppExtensions.toast(R.string.network_Error);
            return false;
        }
        if(!LocalStorage.USER.isProfileVerified()) {
            AppExtensions.toast(R.string.yourProfileNotVerified);
            return false;
        }

        if(chatGroup == null) {
            AppExtensions.toast(R.string.sendingMessageFailed);
            getChatGroup();
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            try {
                Bitmap mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData());
                if(mBitmap == null) return;
                uploadFile(AppExtensions.getBitmapData(mBitmap, 1024));
            }
            catch (IOException ex) {
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.failureToUpload, R.string.retry, CustomSnackBar.Duration.LONG).show();
                ex.printStackTrace();
            }
        }
        else if(requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            try {
                if(photoUri == null) return;
                uploadFile(AppExtensions.getUriData(photoUri));
                photoUri = null;
            }
            catch (Exception ex){
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.failureToUpload, R.string.retry, CustomSnackBar.Duration.LONG).show();
                ex.printStackTrace();
            }
        }
    }

    private void pickFromGallery(int chooserTitle) {
        if (!new PermissionManager(PermissionManager.Permission.GALLERY, true, response -> pickFromGallery(chooserTitle)).isGranted()) return;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, AppExtensions.getString(chooserTitle)), Constants.GALLERY_REQUEST_CODE);
    }

    private Uri photoUri = null;
    private void captureByCamera() {
        if (!new PermissionManager(PermissionManager.Permission.CAMERA, true, response -> captureByCamera() ).isGranted()) return;
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        java.io.File file = new java.io.File(activity.getExternalCacheDir(), (UUID.randomUUID() + ".jpg"));
        if (file.exists()) file.delete();
        photoUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new java.io.File(String.valueOf(file)));
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(takePicture, Constants.CAMERA_REQUEST_CODE);
    }

    private void uploadFile(HashMap<String, Object> photoData){
        if(!isDone()) return;

        progressDialog.setMessage(AppExtensions.getString(R.string.sending));
        progressDialog.show();

        firebaseHelper.uploadPhoto(photoData.get("file"), Photo.OTHER, new FirebaseHelper.OnPhotoUploadListener() {
            @Override
            public void onSuccess(String photoLink) {
                List<File> files = new ArrayList<>();

                File file = new File();
                file.setId(UUID.randomUUID().toString());
                file.setTitle("Attachment" + "_" + System.currentTimeMillis());
                file.setFileType("Image");
                file.setSize((Long) photoData.get("size"));
                file.setPath(photoLink);
                file.sentAt(new Date());
                files.add(file);

                sendMessage(null, files);
            }

            @Override
            public void onFailure() {
                AppExtensions.toast(R.string.sendingMessageFailed);
            }

            @Override
            public void onProgress(double progress) {
                progressDialog.setMessage((int)progress + "% " + AppExtensions.getString(R.string.sending));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(messagesListenerRegistration != null) messagesListenerRegistration.remove();
        if(userInfoListenerRegistration != null) userInfoListenerRegistration.remove();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        this.activity = (Activity) context;
    }

    /**
     *  Hide soft keyboard when click outside
     **/
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(activity, getTheme()) {
            @Override
            public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View v = getCurrentFocus();
                    if (v instanceof AppCompatEditText) {
                        Rect outRect = new Rect();
                        v.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            v.clearFocus();
                            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null)
                                imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
                        }
                    }
                }
                return super.dispatchTouchEvent(event);
            }
        };
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void notifyRequests(List<Request> requests) {
        checkUserHasAccess();
    }
}
