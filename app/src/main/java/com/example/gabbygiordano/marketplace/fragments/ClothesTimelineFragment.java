package com.example.gabbygiordano.marketplace.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.gabbygiordano.marketplace.Item;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by tanvigupta on 7/17/17.
 */

public class ClothesTimelineFragment extends ItemsListFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        populateClothesTimeline();
    }

    public void populateClothesTimeline() {
        ParseQuery<Item> query = ParseQuery.getQuery(Item.class);
        query.include("owner");
        query.orderByDescending("_created_at");
        query.whereEqualTo("type", "Clothes");
        query.findInBackground(new FindCallback<Item>() {
            public void done(List<Item> itemsList, ParseException e) {
                if (e == null) {
                    if (!itemsList.isEmpty()) {
                        addItems(itemsList);
                    }
                } else {
                    Log.d("ClothesFragment", e.getMessage());
                }
            }
        });
    }
}
