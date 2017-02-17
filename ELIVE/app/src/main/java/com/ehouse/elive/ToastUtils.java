package com.ehouse.elive;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static Toast toast;
    /**
     * 单例吐司
     *
     * @param context
     * @param msg
     */
    public static void showToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        toast.setText(msg);
        toast.show();
    }
}
