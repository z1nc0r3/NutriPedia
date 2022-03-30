package com.zincore.nutripedia;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetFoodImage extends AsyncTask<String, Void, String> {

    private final WeakReference<ImageView> foodImageRef;
    private final WeakReference<Context> contextRef;
    int height;

    GetFoodImage(WeakReference<ImageView> foodImageRef, WeakReference<Context> contextRef) {
        this.foodImageRef = foodImageRef;
        this.contextRef = contextRef;

        height = contextRef.get().getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        Glide.with(contextRef.get().getApplicationContext()).load(R.drawable.low).transform(new CenterCrop()).override(height / 4, height / 4).into(foodImageRef.get());

    }

    @Override
    protected String doInBackground(String... query) {
        String resBody;

        OkHttpClient client = new OkHttpClient();
        String url = "https://pixabay.com/api/?key=26182469-fc6c96051a4ec830620d8f4ab&q=" + query[0].replace(" ", "+") + "&image_type=photo&category=food&editors_choice=true";

        Request request = new Request.Builder()
                .url(url)
                .get()
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

        String url = "";

        try {
            JSONObject jsonFullResponse = new JSONObject(response);

            JSONArray results = (JSONArray) jsonFullResponse.get("hits");
            url = results.getJSONObject(0).getString("webformatURL");

        } catch (JSONException ignored) {
            // ignored
        }

        if (response.equals("no_internet_error"))
            Toast.makeText(foodImageRef.get().getContext(), R.string.no_internet_message, Toast.LENGTH_SHORT).show();
        else if (response.equals("no_details_found"))
            Toast.makeText(foodImageRef.get().getContext(), R.string.no_details_found, Toast.LENGTH_SHORT).show();
        else {
            Glide.with(contextRef.get().getApplicationContext()).load(url).override(height / 4, height / 4).transform(new CenterCrop()).into(foodImageRef.get());
        }
    }
}