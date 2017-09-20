package com.wfkj.android.smartoffice.util;

/**
 * Created by zhuxiaolong on 2017/8/9.
 */

public class StringHelper {


    public static String splitName(String fullname) {
        String[] na = fullname.split("--");
        if (na != null && na.length > 1) {
            return na[1];
        } else {
            return fullname;
        }
    }
}
