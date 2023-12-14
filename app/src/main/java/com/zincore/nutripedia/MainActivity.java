package com.zincore.nutripedia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    final TypedValue value = new TypedValue();
    TextView bigFoodName;
    Button search;
    ImageButton clearText;
    EditText foodName;
    CardView bgcard;
    CardView detailCard, nutriCard;
    ImageView foodImage, background, blackBackground;
    ListView nutritionList, amountList;
    InputMethodManager imm;
    AnimatorSet animatorSet;

    boolean isUp = false;
    boolean isSuccess = false;
    int backCount = 0;
    int width, height;
    long currentTime = 0;
    long timeDiff = 0;

    String[] empty_list = {"Na", "Na", "Na", "Na", "Na", "Na", "Na", "Na", "Na", "Na",};
    String preQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        width = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        height = getApplicationContext().getResources().getDisplayMetrics().heightPixels;

        background = findViewById(R.id.background);

        Glide.with(getApplicationContext()).load(R.drawable.sample_food).override(width, height - getStatusBarHeight()).into(background);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        search = findViewById(R.id.search);
        clearText = findViewById(R.id.clearText);
        foodName = findViewById(R.id.foodName);
        bgcard = findViewById(R.id.cardGroup);
        detailCard = findViewById(R.id.detailCard);
        bigFoodName = findViewById(R.id.bigFoodName);
        foodImage = findViewById(R.id.foodImage);
        nutritionList = findViewById(R.id.nutriList);
        amountList = findViewById(R.id.nutriAmount);
        blackBackground = findViewById(R.id.blackBackground);
        nutriCard = findViewById(R.id.nutriCard);

        detailCard.setVisibility(View.GONE);
        nutriCard.setVisibility(View.GONE);

        foodImage.getLayoutParams().height = height / 4;
        foodImage.getLayoutParams().width = height / 4;

        bigFoodName.setPadding(height / 4 + 60, 0, 0, 0);

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant, value, true);

        detailCard.getLayoutParams().height = height;

        String[] nutrition = {"Sugar (g)", "Fat total (g)", "Fat saturated (g)", "Calories", "Cholesterol (mg)", "Protein (g)", "Carbohydrates total (g)", "Sodium (mg)", "Potassium (mg)", "Fiber (g)"};
        ArrayAdapter<String> arr = new ArrayAdapter<>(getApplicationContext(), R.layout.nutrition_list_layout, nutrition);
        nutritionList.setAdapter(arr);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        animatorSet = slideUpAnimation();

        searchService(search, foodName, clearText);
        getStatusBarHeight();
    }

    @Override
    public void onBackPressed() {
        if (isUp) {
            reverseAnimation();
        } else {
            foodName.setText("");
            if (currentTime == 0) {
                currentTime = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), "Press again to exit.", Toast.LENGTH_SHORT).show();
            }

            backCount++;

            timeDiff = System.currentTimeMillis() - currentTime;

            if (timeDiff < 2000 && backCount == 2) {
                super.onBackPressed();
            } else if (timeDiff > 2000) {
                currentTime = 0;
                backCount = 0;
                Toast.makeText(getApplicationContext(), "Press again to exit.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void searchHandler(String query) {

        imm.hideSoftInputFromWindow(foodName.getWindowToken(), 0);
        foodName.clearFocus();

        ConnectivityManager checkConnection = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isConnected = checkConnection.getActiveNetworkInfo() != null;

        if (!isConnected) {
            Toast.makeText(getApplicationContext(), R.string.no_internet_message, Toast.LENGTH_SHORT).show();
        } else if (!query.isEmpty() && (!query.equals(preQuery) || !isUp || !isSuccess)) {
            GetNutritionDetails getNutritionDetails = new GetNutritionDetails(new WeakReference<>(foodImage), new WeakReference<>(bigFoodName), new WeakReference<>(amountList), new WeakReference<>(getApplicationContext()));
            getNutritionDetails.execute(query);
            preQuery = query;
            isSuccess = getNutritionDetails.getSuccess();

            if (!isUp) {
                detailCard.setVisibility(View.VISIBLE);
                nutriCard.setVisibility(View.VISIBLE);
                animatorSet.start();
                isUp = true;

                getWindow().setStatusBarColor(value.data);
            }

        } else if (!query.equals(preQuery)) {
            Toast.makeText(getApplicationContext(), R.string.invalid_name, Toast.LENGTH_SHORT).show();
        }
    }

    public void searchService(Button search, EditText foodName, ImageButton clearText) {

        foodName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = foodName.getEditableText().toString().trim();
                    searchHandler(query);
                }
                return false;
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = foodName.getEditableText().toString().trim();
                searchHandler(query);
            }
        });

        clearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foodName.setText("");
            }
        });

    }

    public AnimatorSet slideUpAnimation() {

        float moveHeight = -(height / 2f);

        ObjectAnimator anmSearch = ObjectAnimator.ofFloat(search, "translationY", moveHeight + 100);
        ObjectAnimator anmFoodName = ObjectAnimator.ofFloat(foodName, "translationY", moveHeight + 100);
        ObjectAnimator anmClear = ObjectAnimator.ofFloat(clearText, "translationY", moveHeight + 100);
        ObjectAnimator anmCard = ObjectAnimator.ofFloat(bgcard, "translationY", (moveHeight + 56));
        ObjectAnimator anmCardAlpha = ObjectAnimator.ofFloat(bgcard, "alpha", 1);
        ObjectAnimator anmCardScaleY = ObjectAnimator.ofFloat(bgcard, "scaleY", 1.3f);
        ObjectAnimator anmDetailCard = ObjectAnimator.ofFloat(detailCard, "translationY", moveHeight * 1.8f + height / 10f);
        ObjectAnimator anmDetailCardAlpha = ObjectAnimator.ofFloat(detailCard, "alpha", 1);
        ObjectAnimator anmNutriCard = ObjectAnimator.ofFloat(nutriCard, "translationY", moveHeight * 1.8f + height / 10f + height / 3.33f);
        ObjectAnimator anmNutriCardAlpha = ObjectAnimator.ofFloat(nutriCard, "alpha", 1);
        ObjectAnimator anmBackgroundAlpha = ObjectAnimator.ofFloat(blackBackground, "alpha", .5f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(anmSearch).with(anmFoodName).with(anmClear).with(anmCard).with(anmCardAlpha).with(anmCardScaleY).with(anmDetailCard).with(anmDetailCardAlpha).with(anmNutriCard).with(anmNutriCardAlpha).with(anmBackgroundAlpha);

        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new DecelerateInterpolator());

        return animatorSet;
    }

    public void reverseAnimation() {

        ObjectAnimator anmSearch = ObjectAnimator.ofFloat(search, "translationY", 0);
        ObjectAnimator anmFoodName = ObjectAnimator.ofFloat(foodName, "translationY", 0);
        ObjectAnimator anmClear = ObjectAnimator.ofFloat(clearText, "translationY", 0);
        ObjectAnimator anmCard = ObjectAnimator.ofFloat(bgcard, "translationY", 0);
        ObjectAnimator anmCardAlpha = ObjectAnimator.ofFloat(bgcard, "alpha", 0.8f);
        ObjectAnimator anmCardScaleY = ObjectAnimator.ofFloat(bgcard, "scaleY", 1);
        ObjectAnimator anmDetailCard = ObjectAnimator.ofFloat(detailCard, "translationY", 0);
        ObjectAnimator anmDetailCardAlpha = ObjectAnimator.ofFloat(detailCard, "alpha", 0);
        ObjectAnimator anmNutriCard = ObjectAnimator.ofFloat(nutriCard, "translationY", 0);
        ObjectAnimator anmNutriCardAlpha = ObjectAnimator.ofFloat(nutriCard, "alpha", 0);
        ObjectAnimator anmBackgroundAlpha = ObjectAnimator.ofFloat(blackBackground, "alpha", 0.1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(anmSearch).with(anmFoodName).with(anmClear).with(anmCard).with(anmCardAlpha).with(anmCardScaleY).with(anmDetailCard).with(anmDetailCardAlpha).with(anmNutriCard).with(anmNutriCardAlpha).with(anmBackgroundAlpha);

        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
        getWindow().setStatusBarColor(Color.BLACK);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                detailCard.setVisibility(View.GONE);
                nutriCard.setVisibility(View.GONE);
                Glide.with(getApplicationContext()).load(R.drawable.low).transform(new CenterCrop()).override(height / 4, height / 4).into(foodImage);
                bigFoodName.setText("");
                amountList.setAdapter(new ArrayAdapter<>(amountList.getContext(), R.layout.nutrition_amount_layout, empty_list));
            }
        });

        isUp = false;
    }

    private int getStatusBarHeight() {
        int height;
        Resources myResources = getResources();
        int idStatusBarHeight = myResources.getIdentifier("status_bar_height", "dimen", "android");
        height = getResources().getDimensionPixelSize(idStatusBarHeight);

        return (idStatusBarHeight > 0) ? height : 0;
    }
}