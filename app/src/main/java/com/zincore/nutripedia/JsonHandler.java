package com.zincore.nutripedia;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class JsonHandler {
    private final WeakReference<ListView> amountListRef;
    private final WeakReference<Context> contextRef;
    String[] nutritions = {"sugar_g", "fat_total_g", "fat_saturated_g", "calories", "cholesterol_mg", "protein_g", "carbohydrates_total_g", "sodium_mg", "potassium_mg", "fiber_g"};
    String[] empty_list = {"Na", "Na", "Na", "Na", "Na", "Na", "Na", "Na", "Na", "Na"};
    ArrayList<String> amounts = new ArrayList<>();
    private JSONArray items;

    JsonHandler(String response, WeakReference<ListView> amountListRef, WeakReference<Context> contextRef) {
        this.amountListRef = amountListRef;
        this.contextRef = contextRef;

        try {
            JSONObject jsonFullResponse = new JSONObject(response);
            items = (JSONArray) jsonFullResponse.get("items");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        try {
            return items.getJSONObject(0).getString("name");
        } catch (JSONException e) {
            return "none";
        }
    }

    public void getAmounts() {
        for (String nutrition : nutritions) {
            try {
                amounts.add(items.getJSONObject(0).getString(nutrition));
            } catch (JSONException ignored) {
                // ignored
            }
        }

        ArrayAdapter<String> arr = new ArrayAdapter<>(contextRef.get().getApplicationContext(), R.layout.nutrition_amount_layout, amounts);
        amountListRef.get().setAdapter(arr);
    }

    public void setEmptyAmounts() {
        amountListRef.get().setAdapter(new ArrayAdapter<>(contextRef.get().getApplicationContext(), R.layout.nutrition_amount_layout, empty_list));
    }
}