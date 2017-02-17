package com.ehouse.elive;

/**
 * Created by Administrator on 2016/9/20.
 */
public class StringUtils {
    //判断字符串是否为空
    public static boolean isEmpty(String value) {
        if (value != null && !"".equalsIgnoreCase(value.trim()) && !"null".equalsIgnoreCase(value.trim())) {
            return false;
        } else {
            return true;
        }
    }

}
