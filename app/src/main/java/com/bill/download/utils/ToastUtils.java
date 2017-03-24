package com.bill.download.utils;

import android.content.Context;
import android.widget.Toast;

import java.io.Serializable;

/**
 * 吐司工具类
 */
public class ToastUtils {

    private static Toast toast;

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showShort(Context context, Serializable message) {
        if (null == toast) {
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }

        if (message instanceof String) {
            toast.setText((String) message);
        } else if (message instanceof Integer) {
            toast.setText((Integer) message);
        }

        toast.show();
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showLong(Context context, Serializable message) {
        if (null == toast) {
            toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
        }

        if (message instanceof String) {
            toast.setText((String) message);
        } else if (message instanceof Integer) {
            toast.setText((Integer) message);
        }

        toast.show();
    }

    /**
     * 取消toast，当前管理的任何toast
     */
    public static void hideToast() {
        if (null != toast) {
            toast.cancel();
        }
    }
}
