package com.march.piceditor.utils;

import android.util.Log;

/**
 * CreateAt : 2017/11/29
 * Describe : 日志类
 *
 * @author chendong
 */
public class LogUtils {

    private static boolean DEBUG = true;
    private static String TAG = "LogUtils";

    public LogUtils() {
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    public static void setDefTag(String defTag) {
        TAG = defTag;
    }

    private static boolean checkMsgNotNull(Object msg) {
        if(msg == null) {
            Log.e(TAG, "log msg is null");
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkMsgNotNull(String tag, Object msg) {
        if(msg == null) {
            Log.e(getTag(tag), "log msg is null");
            return false;
        } else {
            return true;
        }
    }

    private static String getTag(String tag) {
        return tag == null?TAG:tag;
    }

    public static void i(Object msg) {
        if(DEBUG && checkMsgNotNull(msg)) {
            Log.i(TAG, msg.toString());
        }

    }

    public static void i(String tag, Object msg) {
        if(DEBUG && checkMsgNotNull(tag, msg)) {
            Log.i(getTag(tag), msg.toString());
        }

    }

    public static void e(Object msg) {
        if(DEBUG && checkMsgNotNull(msg)) {
            Log.e(TAG, msg.toString());
        }

    }

    public static void e(String tag, Object msg) {
        if(DEBUG && checkMsgNotNull(tag, msg)) {
            Log.e(getTag(tag), msg.toString());
        }

    }
}
