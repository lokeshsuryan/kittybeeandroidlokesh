package com.kittyapplication.core.utils;

import android.os.Bundle;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Created by MIT on 8/19/2016.
 */
public class StringUtils {
    public static void printBundle(Bundle bundle) {
        if (bundle != null) {
            Set<String> set = bundle.keySet();
            System.out.print("Bundle  key => value ");
            for (String s : set) {
                System.out.print(s + " => " + bundle.get(s));
            }
        }
    }

    public static String toJson(Class clazz, Object object) {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                sb.append("\"" + field.getName() + "\"").append(":");
                if (field.get(object) == null) {
                    sb.append("\"\"").append(",");
                } else {
                    sb.append("\"" + field.get(object).toString() + "\"").append(",");
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        String str = sb.substring(0, sb.length() - 1);
        sb = new StringBuffer(str);
        sb.append("}");
        return sb.toString();
    }
}
