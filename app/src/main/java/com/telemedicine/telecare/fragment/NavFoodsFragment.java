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
import com.telemedicine.telecare.adapter.FoodsAdapter;
import com.telemedicine.telecare.helper.FirebaseHelper;
import com.telemedicine.telecare.model.food.Food;
import com.telemedicine.telecare.util.Constants;
import com.telemedicine.telecare.util.extensions.AppExtensions;
import java.util.ArrayList;
import java.util.List;

public class NavFoodsFragment extends Fragment {

    private View                    rootView;

    private RecyclerView            rcv_Foods;
    private FoodsAdapter            foods_Adapter;

    private LinearLayoutCompat      empty_Layout;
    private LottieAnimationView     empty_Icon;
    private AppCompatTextView       empty_Title;
    private AppCompatTextView       empty_Subtitle;

    private ListenerRegistration    foodsListenerRegistration;
    private FirebaseHelper          firebaseHelper;
    private LoadingFragment         loading;

    public NavFoodsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_nav_foods, container, false);
        initId();
        init();
        return rootView;
    }

    private void initId() {
        rcv_Foods = rootView.findViewById(R.id.food_Foods_Rcv);
        empty_Layout = rootView.findViewById(R.id.food_Empty_Layout);
        empty_Icon = rootView.findViewById(R.id.empty_Icon_Animation);
        empty_Title = rootView.findViewById(R.id.empty_Title_Tv);
        empty_Subtitle = rootView.findViewById(R.id.empty_Subtitle_Tv);
    }

    private void init() {
        firebaseHelper = new FirebaseHelper();

        empty_Icon.setAnimation("lottie/empty_foods.json");
        empty_Title.setText(AppExtensions.getString(R.string.emptyFoodsTitle));
        empty_Subtitle.setText(AppExtensions.getString(R.string.emptyFoodsSubtitle));

        setAdapter();

        getFoods();
    }

    private void setAdapter(){
        foods_Adapter = new FoodsAdapter();
        rcv_Foods.setAdapter(foods_Adapter);
        foods_Adapter.setOnFoodListener(new FoodsAdapter.OnFoodListener() {
            @Override
            public void onSelect(Food food) {
                AddFoodFragment.show(food);
            }

            @Override
            public void onRemove(Food food) {
                AlertDialogFragment.show(R.string.removeFood, (AppExtensions.getString(R.string.youWantToRemove) + " " + food.getName()), R.string.cancel, R.string.remove)
                        .setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
                            @Override
                            public void onLeftButtonClick() {}

                            @Override
                            public void onRightButtonClick() {
                                loading = LoadingFragment.show();
                                firebaseHelper.setDocumentData(FirebaseFirestore.getInstance()
                                                .collection(FirebaseHelper.FOODS_TABLE)
                                                .document(food.getId())
                                                .delete(),
                                        new FirebaseHelper.OnFirebaseUpdateListener() {
                                            @Override
                                            public void onSuccess() { AppExtensions.dismissLoading(loading); }

                                            @Override
                                            public void onFailure() { AppExtensions.dismissLoading(loading); }

                                            @Override
                                            public void onCancelled() { AppExtensions.dismissLoading(loading); }
                                        });
                            }
                        });
            }
        });
    }

    private void getFoods() {
        loading = LoadingFragment.show();

        EventListener<QuerySnapshot> foodsEventListener = (requestSnapshot, error) -> {
            AppExtensions.dismissLoading(loading);

            /** Error & Null Data Checking **/
            if (error != null) {
                Log.e(Constants.TAG, "Chat Group Error, Reason: " + error.getCode(), error);
                foods_Adapter.setFoods(new ArrayList<>());
                empty_Layout.setVisibility(View.VISIBLE);
                return;
            }
            else if (requestSnapshot == null || requestSnapshot.isEmpty()) {
                Log.e(Constants.TAG, "No Foods");
                foods_Adapter.setFoods(new ArrayList<>());
                empty_Layout.setVisibility(View.VISIBLE);
                return;
            }

            /** Get All Foods **/
            List<Food> foods = new ArrayList<>();
            for (QueryDocumentSnapshot snapshot : requestSnapshot) {
                Food food = snapshot.toObject(Food.class);
                foods.add(food);
            }

            foods_Adapter.setFoods(foods);
            empty_Layout.setVisibility(foods.size() == 0 ? View.VISIBLE : View.GONE);
        };

        foodsListenerRegistration = FirebaseFirestore.getInstance()
                .collection(FirebaseHelper.FOODS_TABLE)
                .addSnapshotListener(foodsEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(foodsListenerRegistration != null) foodsListenerRegistration.remove();
    }
}