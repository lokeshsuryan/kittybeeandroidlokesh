package com.kittyapplication.custom;

import com.kittyapplication.model.ChatData;

import java.util.Comparator;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 8/11/16.
 */

public class ChatComparator implements Comparator<ChatData> {

    @Override
    public int compare(ChatData o1, ChatData o2) {
        int nameComp = 0;
        try {
            String name1 = o1.getName().toLowerCase();
            String name2 = o2.getName().toLowerCase();
            nameComp = name1.compareTo(name2);

            return nameComp;
        } catch (Exception e) {
            return nameComp;
        }
    }
}