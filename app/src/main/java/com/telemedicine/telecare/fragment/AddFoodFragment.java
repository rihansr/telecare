package com.telemedicine.telecare.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.telemedicine.telecare.BuildConfig;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.base.AppController;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.helper.PermissionManager;
import com.telemedicine.telecare.model.food.Food;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.CustomPopup;
import com.telemedicine.telecare.util.CustomSnackBar;
import com.telemedicine.telecare.util.enums.Photo;
import com.telemedicine.telecare.util.extensions.AppExtensions;

import org.json.JSONException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

@SuppressLint("SetTextI18n, ClickableViewAccessibility")
public class AddFoodFragment extends DialogFragment {

    private static final String     TAG = AddFoodFragment.class.getSimpleName();
    private Activity                activity;
    private Context                 context;
    private AppCompatImageView      food_Photo;
    private RelativeLayout          photoUpload_Button;
    private AppCompatEditText       name_Input;
    private AppCompatEditText       type_Input;
    private AppCompatEditText       quantity_Input;
    private AppCompatEditText       quantityUnit_Input;
    private AppCompatEditText       calories_input;
    private AppCompatEditText       fat_input;
    private AppCompatEditText       carboHydrate_input;
    private AppCompatEditText       sugars_input;
    private AppCompatEditText       protein_input;
    private AppCompatEditText       vitaminA_input;
    private AppCompatEditText       vitaminC_input;
    private AppCompatEditText       calcium_input;
    private AppCompatEditText       saturatedFat_input;
    private AppCompatEditText       cholesterol_input;
    private AppCompatEditText       additionalInfo_input;
    private AppCompatButton         add_Button;
    private AppCompatImageView      back_Button;
    private AppCompatImageView      more_Button;
    private Food                    food;

    private LoadingFragment         loading;
    private FirebaseHelper          firebaseHelper;
    private HashMap<String, Object> photoData = null;

    public static AddFoodFragment show(){
        AddFoodFragment fragment = new AddFoodFragment();
        fragment.show(((AppCompatActivity) AppController.getActivity()).getSupportFragmentManager(), TAG);
        return fragment;
    }

    public static AddFoodFragment show(Food food){
        AddFoodFragment fragment = new AddFoodFragment();
        if(food != null){
            Bundle args = new Bundle();
            args.putSerializable(Constants.FOOD_BUNDLE_KEY, food);
            fragment.setArguments(args);
        }
        fragment.show(((AppCompatActivity) AppController.getActivity()).getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        AppExtensions.halfScreenDialog(getDialog());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_add_food, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initId(view);

        init();
    }

    private void initId(View view) {
        food_Photo = view.findViewById(R.id.food_Photo_Iv);
        photoUpload_Button = view.findViewById(R.id.food_UploadPhoto_Button);
        name_Input = view.findViewById(R.id.food_Name_Input);
        type_Input = view.findViewById(R.id.food_Type_Input);
        quantity_Input = view.findViewById(R.id.food_Quantity_Input);
        quantityUnit_Input = view.findViewById(R.id.food_QuantityUnit_Input);
        quantityUnit_Input.setInputType(InputType.TYPE_NULL);
        calories_input = view.findViewById(R.id.food_Calories_Input);
        fat_input = view.findViewById(R.id.food_Fat_Input);
        carboHydrate_input = view.findViewById(R.id.food_CarboHydrate_Input);
        sugars_input = view.findViewById(R.id.food_Sugars_Input);
        protein_input = view.findViewById(R.id.food_Protein_Input);
        vitaminA_input = view.findViewById(R.id.food_VitaminA_Input);
        vitaminC_input = view.findViewById(R.id.food_VitaminC_Input);
        calcium_input = view.findViewById(R.id.food_Calcium_Input);
        saturatedFat_input = view.findViewById(R.id.food_SaturatedFat_Input);
        cholesterol_input = view.findViewById(R.id.food_Cholesterol_Input);
        additionalInfo_input = view.findViewById(R.id.food_AdditionalInfo_Input);
        add_Button = view.findViewById(R.id.food_Add_Button);
        more_Button = view.findViewById(R.id.food_More_Button);
        back_Button = view.findViewById(R.id.food_Back_Button);
    }

    private void init() {
        if(getArguments() != null && getArguments().containsKey(Constants.FOOD_BUNDLE_KEY)) {
            food = (Food) getArguments().getSerializable(Constants.FOOD_BUNDLE_KEY);
            getArguments().remove(Constants.FOOD_BUNDLE_KEY);
        }

        firebaseHelper = new FirebaseHelper();

        if(food != null && food.getPhoto() != null) AppExtensions.loadPhoto(food_Photo, food.getPhoto(), R.dimen.icon_Size_XXXX_Large, R.drawable.ic_placeholder_large);

        food_Photo.setOnLongClickListener(v -> {
            if(food != null && food.getPhoto() != null) PhotoViewFragment.show(food.getPhoto());
            return false;
        });

        photoUpload_Button.setOnClickListener(view ->
                PhotoActionFragment.show().setOnActionListener((dialog, isCapture) -> {
                    if (isCapture) captureByCamera();
                    else pickFromGallery(R.string.select_FoodPhoto);
                    dialog.dismiss();
                })
        );

        back_Button.setOnClickListener(view -> dismiss());

        more_Button.setVisibility(food != null ? View.VISIBLE : View.GONE);

        more_Button.setOnClickListener(v ->
                new CustomPopup(v, new String[]{AppExtensions.getString(R.string.delete)}, CustomPopup.Popup.MENU)
                        .setOnPopupListener((position, item) -> {
                            if (position == 0) deleteFood(food);
                        })
        );

        DecimalFormat df = new DecimalFormat("###.#");

        name_Input.setText(food == null || food.getName() == null ? "" : food.getName());
        type_Input.setText(food == null || food.getType() == null ? "" : food.getType());
        quantity_Input.setText(food == null || food.getQuantity() == null ? "" : df.format(food.getQuantity()));
        quantityUnit_Input.setText(food == null || food.getQuantityUnit() == null ? "g" : food.getQuantityUnit());
        calories_input.setText(food == null || food.getCalories() == null ? "" : df.format(food.getCalories()));
        fat_input.setText(food == null || food.getFat() == null ? "" : df.format(food.getFat()));
        sugars_input.setText(food == null || food.getSugars() == null ? "" : df.format(food.getSugars()));
        protein_input.setText(food == null || food.getProtein() == null ? "" : df.format(food.getProtein()));
        vitaminA_input.setText(food == null || food.getVitaminA() == null ? "" : df.format(food.getVitaminA()));
        vitaminC_input.setText(food == null || food.getVitaminC() == null ? "" : df.format(food.getVitaminC()));
        calcium_input.setText(food == null || food.getCalcium() == null ? "" : df.format(food.getCalcium()));
        saturatedFat_input.setText(food == null || food.getSaturatedFat() == null ? "" : df.format(food.getSaturatedFat()));
        carboHydrate_input.setText(food == null || food.getCarboHydrate() == null ? "" : df.format(food.getCarboHydrate()));
        cholesterol_input.setText(food == null || food.getCholesterol() == null ? "" : df.format(food.getCholesterol()));
        additionalInfo_input.setText(food == null || food.getAdditionalInfo() == null ? "" : food.getAdditionalInfo());

        quantityUnit_Input.setOnTouchListener((v, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                new CustomPopup(v, AppExtensions.getStringArray(R.array.quantityUnit), CustomPopup.Popup.WINDOW)
                        .setOnPopupListener((position, item) -> quantityUnit_Input.setText(item));
            }
            return false;
        });

        add_Button.setOnClickListener(view -> {
            String name = Objects.requireNonNull(name_Input.getText()).toString().trim();
            String type = Objects.requireNonNull(type_Input.getText()).toString().trim();
            String quantity = Objects.requireNonNull(quantity_Input.getText()).toString().trim();
            String quantityUnit = Objects.requireNonNull(quantityUnit_Input.getText()).toString().trim();
            String calories = Objects.requireNonNull(calories_input.getText()).toString().trim();
            String fat = Objects.requireNonNull(fat_input.getText()).toString().trim();
            String sugars = Objects.requireNonNull(sugars_input.getText()).toString().trim();
            String protein = Objects.requireNonNull(protein_input.getText()).toString().trim();
            String vitaminA = Objects.requireNonNull(vitaminA_input.getText()).toString().trim();
            String vitaminC = Objects.requireNonNull(vitaminC_input.getText()).toString().trim();
            String calcium = Objects.requireNonNull(calcium_input.getText()).toString().trim();
            String saturatedFat = Objects.requireNonNull(saturatedFat_input.getText()).toString().trim();
            String carboHydrate = Objects.requireNonNull(carboHydrate_input.getText()).toString().trim();
            String cholesterol = Objects.requireNonNull(cholesterol_input.getText()).toString().trim();
            String additionalInfo = Objects.requireNonNull(additionalInfo_input.getText()).toString().trim();

            if (!AppExtensions.isInputValid(name_Input, R.string.foodName_Error)
                    || !AppExtensions.isInputValid(quantity_Input, R.string.foodQuantity_Error)
            ) return;

            boolean isUpdate = this.food != null && this.food.getId() != null;

            Food food = new Food();
            food.setId(isUpdate ? this.food.getId() : UUID.randomUUID().toString());
            food.setName(name);
            food.setType(type);
            food.setQuantity(TextUtils.isEmpty(quantity) ? null : Double.parseDouble(quantity));
            food.setQuantityUnit(quantityUnit);
            food.setCalories(TextUtils.isEmpty(calories) ? null : Double.parseDouble(calories));
            food.setFat(TextUtils.isEmpty(fat) ? null : Double.parseDouble(fat));
            food.setSugars(TextUtils.isEmpty(sugars) ? null : Double.parseDouble(sugars));
            food.setProtein(TextUtils.isEmpty(protein) ? null : Double.parseDouble(protein));
            food.setVitaminA(TextUtils.isEmpty(vitaminA) ? null : Double.parseDouble(vitaminA));
            food.setVitaminC(TextUtils.isEmpty(vitaminC) ? null : Double.parseDouble(vitaminC));
            food.setCalcium(TextUtils.isEmpty(calcium) ? null : Double.parseDouble(calcium));
            food.setSaturatedFat(TextUtils.isEmpty(saturatedFat) ? null : Double.parseDouble(saturatedFat));
            food.setCarboHydrate(TextUtils.isEmpty(carboHydrate) ? null : Double.parseDouble(carboHydrate));
            food.setCholesterol(TextUtils.isEmpty(cholesterol) ? null : Double.parseDouble(cholesterol));
            food.setAdditionalInfo(additionalInfo);

            loading = LoadingFragment.show();
            if(photoData != null && photoData.get("file") != null) uploadFoodPhoto(food, isUpdate);
            else addFood(food, isUpdate);
        });
    }

    private void uploadFoodPhoto(Food food, boolean isUpdate){
        firebaseHelper.uploadPhoto(photoData.get("file"), Photo.FOOD, new FirebaseHelper.OnPhotoUploadListener() {
            @Override
            public void onSuccess(String photoLink) {
                food.setPhoto(photoLink);
                addFood(food, isUpdate);
            }

            @Override
            public void onFailure() {
                addFood(food, isUpdate);
            }

            @Override
            public void onProgress(double progress) {}
        });
    }

    private void addFood(Food food, boolean isUpdate){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference foodsReference = db.collection(FirebaseHelper.FOODS_TABLE).document(food.getId());

        HashMap<String, Object> foodMap = new HashMap<>();
        if(isUpdate){
            try {
                foodMap = AppExtensions.jsonToMap(new Gson().toJson(food));
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }

        firebaseHelper.setDocumentData(isUpdate ? foodsReference.update(foodMap) : foodsReference.set(food),
                new FirebaseHelper.OnFirebaseUpdateListener() {
                    @Override
                    public void onSuccess() {
                        AppExtensions.dismissLoading(loading);
                        if(isUpdate){
                            dismiss();
                        }
                        else {
                            updateUi();
                            new CustomSnackBar(AppExtensions.getRootView(getDialog()), (food.getName() + " " + AppExtensions.getString(R.string.addSuccessfully)), R.string.okay, CustomSnackBar.Duration.SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure() {
                        AppExtensions.dismissLoading(loading);
                        new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.failureMessage, R.string.okay, CustomSnackBar.Duration.SHORT).show();
                    }

                    @Override
                    public void onCancelled() { AppExtensions.dismissLoading(loading); }
                });
    }

    private void deleteFood(Food food){
        loading = LoadingFragment.show();
        firebaseHelper.setDocumentData(FirebaseFirestore.getInstance()
                        .collection(FirebaseHelper.FOODS_TABLE)
                        .document(food.getId())
                        .delete(),
                new FirebaseHelper.OnFirebaseUpdateListener() {
                    @Override
                    public void onSuccess() { AppExtensions.dismissLoading(loading); dismiss(); }

                    @Override
                    public void onFailure() { AppExtensions.dismissLoading(loading); dismiss(); }

                    @Override
                    public void onCancelled() { AppExtensions.dismissLoading(loading); dismiss(); }
                });
    }

    void updateUi(){
        photoData = null;
        food_Photo.setImageResource(R.drawable.ic_placeholder_large);
        name_Input.setText(null);
        type_Input.setText(null);
        quantity_Input.setText(null);
        quantityUnit_Input.setText(null);
        calories_input.setText(null);
        fat_input.setText(null);
        carboHydrate_input.setText(null);
        sugars_input.setText(null);
        protein_input.setText(null);
        vitaminA_input.setText(null);
        vitaminC_input.setText(null);
        calcium_input.setText(null);
        saturatedFat_input.setText(null);
        cholesterol_input.setText(null);
        additionalInfo_input.setText(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            try {
                Bitmap mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData());
                if(mBitmap == null) return;
                food_Photo.setImageBitmap(mBitmap);
                photoData = AppExtensions.getBitmapData(mBitmap, 1024);
            }
            catch (IOException ex) {
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.failureToUpload, R.string.retry, CustomSnackBar.Duration.LONG).show();
                ex.printStackTrace();
            }
        }
        else if(requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            try {
                if(photoUri == null) return;
                food_Photo.setImageURI(photoUri);
                photoData = AppExtensions.getUriData(photoUri);
                photoUri = null;
            }
            catch (Exception ex){
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.failureToUpload, R.string.retry, CustomSnackBar.Duration.LONG).show();
                ex.printStackTrace();
            }
        }
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

    private void pickFromGallery(int chooserTitle) {
        if (!new PermissionManager(PermissionManager.Permission.GALLERY, true, response -> pickFromGallery(chooserTitle)).isGranted()) return;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, AppExtensions.getString(chooserTitle)), Constants.GALLERY_REQUEST_CODE);
    }

    /**
     *  Context Bind
     **/
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
}
