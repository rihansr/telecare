package com.telemedicine.telecare.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.telemedicine.telecare.R;
import com.telemedicine.telecare.fragment.AlertDialogFragment;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import java.util.List;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static androidx.core.content.ContextCompat.checkSelfPermission;

public class PermissionManager {

    public enum Permission {
        LOCATION, PHONE, CAMERA, GALLERY
    }

    private Context     context;
    private Activity    activity;
    private Permission  permission;
    private boolean     isGranted;
    private boolean     showDialog;
    private OnPermissionListener permissionListener;

    public PermissionManager() {
        this.activity = AppController.getActivity();
    }

    public PermissionManager(Permission permission) {
        this.activity = AppController.getActivity();
        this.permission = permission;
    }

    public PermissionManager(Permission permission, boolean showDialog) {
        this.context = AppController.getContext();
        this.activity = AppController.getActivity();
        this.permission = permission;
        this.isGranted = false;
        this.showDialog = showDialog;
    }

    public PermissionManager(Permission permission, boolean showDialog, OnPermissionListener permissionListener) {
        this.context = AppController.getContext();
        this.activity = AppController.getActivity();
        this.permission = permission;
        this.isGranted = false;
        this.showDialog = showDialog;
        this.permissionListener = permissionListener;
    }

    public boolean isGranted(){
        switch (permission){
            case LOCATION:
                isGranted = checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED || checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED;
                if(!isGranted && showDialog) showPermissionDialog(ACCESS_FINE_LOCATION);
                break;

            case PHONE:
                isGranted = checkSelfPermission(context, CALL_PHONE) == PERMISSION_GRANTED;
                if(!isGranted && showDialog) showPermissionDialog(CALL_PHONE);
                break;

            case CAMERA:
                isGranted = checkSelfPermission(context, CAMERA) == PERMISSION_GRANTED;
                if(!isGranted && showDialog) showPermissionDialog(CAMERA);
                break;

            case GALLERY:
                isGranted = checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED || checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
                if(!isGranted && showDialog) showPermissionDialog(WRITE_EXTERNAL_STORAGE);
                break;
        }

        return isGranted;
    }

    public void showPermissionDialogs(){
        Dexter.withActivity(activity)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    @SuppressLint("MissingPermission")
                    public void onPermissionsChecked(MultiplePermissionsReport report) {}

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {}
                })
                .check();
    }

    public void showPermissionDialog(String manifestPermission){
        Dexter.withActivity(activity).withPermission(manifestPermission).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                if(permissionListener != null) permissionListener.onPermissionGranted(response);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                showPermissionSettingDialog();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    public interface OnPermissionListener{
        void onPermissionGranted(PermissionGrantedResponse response);
    }

    private void showPermissionSettingDialog(){
        String title = null;    String message = null;

        switch (permission){
            case LOCATION:
                title = AppExtensions.getString(R.string.locationPermission);  message = AppExtensions.getString(R.string.locationPermissionMessage);
                break;

            case PHONE:
                title = AppExtensions.getString(R.string.callPermission);      message = AppExtensions.getString(R.string.callPermissionMessage);
                break;

            case CAMERA:
                title = AppExtensions.getString(R.string.cameraPermission);    message = AppExtensions.getString(R.string.cameraPermissionMessage);
                break;

            case GALLERY:
                title = AppExtensions.getString(R.string.galleryPermission);   message = AppExtensions.getString(R.string.galleryPermissionMessage);
                break;
        }

        AlertDialogFragment.show(title, message, R.string.cancel, R.string.openSettings)
                .setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
                    @Override public void onLeftButtonClick() {}
                    @Override public void onRightButtonClick() { goToPermissionSetting(); }
                });
    }

    public void goToPermissionSetting(){
        Activity activity = AppController.getActivity();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public void goToNotificationServiceSetting(){
        Activity activity = AppController.getActivity();
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

        /**
         * for Android 5-7
         **/
        intent.putExtra("app_package", activity.getPackageName());
        intent.putExtra("app_uid", activity.getApplicationInfo().uid);

        /**
         * for Android 8 and above
         **/
        intent.putExtra("android.provider.extra.APP_PACKAGE", activity.getPackageName());

        activity.startActivity(intent);
    }

    public void goToBackgroundServiceSetting(){
        Activity activity = AppController.getActivity();

        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
            }
            catch (ActivityNotFoundException e) {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    activity.startActivity(intent);
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }
}
