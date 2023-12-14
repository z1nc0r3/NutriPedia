package com.zincore.nutripedia;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetNutritionDetails extends AsyncTask<String, Void, String> {

    static boolean isPreGood = false;
    static boolean isSuccess = false;
    private final WeakReference<ImageView> foodImageRef;
    private final WeakReference<TextView> bigFoodNameRef;
    private final WeakReference<ListView> amountListRef;
    private final WeakReference<Context> contextRef;
    private final OkHttpClient client = new OkHttpClient();
    private String query;

    GetNutritionDetails(WeakReference<ImageView> foodImageRef, WeakReference<TextView> bigFoodNameRef, WeakReference<ListView> amountListRef, WeakReference<Context> contextRef) {
        this.foodImageRef = foodImageRef;
        this.bigFoodNameRef = bigFoodNameRef;
        this.amountListRef = amountListRef;
        this.contextRef = contextRef;
    }

    @Override
    protected String doInBackground(String... query) {
        this.query = query[0];
        String resBody;

        String url = "https://calorieninjas.p.rapidapi.com/v1/nutrition?query=" + query[0];

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-host", "calorieninjas.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "f424651ee7msh036ca3719beb42dp1ab10ajsn4831784ef652")
                .build();

        try {
            Response response = client.newCall(request).execute();
            resBody = response.body().string();
            response.close();
            return resBody;
        } catch (IOException e) {
            return "no_internet_error";
        } catch (NullPointerException e) {
            return "no_details_found";
        }
    }

    @Override
    protected void onPostExecute(String response) {
        if (response.equals("no_internet_error")) {
            Toast.makeText(contextRef.get().getApplicationContext(), "No internet connection found.", Toast.LENGTH_SHORT).show();
        } else if (response.equals("no_details_found")) {
            Toast.makeText(contextRef.get().getApplicationContext(), R.string.no_details_found, Toast.LENGTH_SHORT).show();
        } else {
            isSuccess = true;
            JsonHandler jsonHandler = new JsonHandler(response, amountListRef, contextRef);

            if (!jsonHandler.getName().equals("none")) {
                isPreGood = true;
                GetFoodImage getFoodImage = new GetFoodImage(foodImageRef, contextRef);
                getFoodImage.execute(query);
                jsonHandler.getAmounts();

                if (jsonHandler.getName().length() > 8)
                    bigFoodNameRef.get().setTextSize(17);
                else
                    bigFoodNameRef.get().setTextSize(20);

                bigFoodNameRef.get().setText(jsonHandler.getName());

            } else if (jsonHandler.getName().equals("Error")) {
                Toast.makeText(contextRef.get().getApplicationContext(), "No internet connection found.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(contextRef.get().getApplicationContext(), "Please enter a valid name.", Toast.LENGTH_SHORT).show();
                if (!isPreGood)
                    jsonHandler.setEmptyAmounts();
            }
        }
    }

    public boolean getSuccess() {
        return isSuccess;
    }

}