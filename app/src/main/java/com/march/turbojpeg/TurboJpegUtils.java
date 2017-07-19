package com.march.turbojpeg;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import com.march.dev.utils.BitmapUtils;

/**
 * CreateAt : 7/19/17
 * Describe :
 *
 * @author chendong
 */
public class TurboJpegUtils {


    private static boolean isSupportArmeabi = true;

    static {
        try {
            System.loadLibrary("compress");
        } catch (Exception e) {
            e.printStackTrace();
            isSupportArmeabi = false;
        }
    }


    public static boolean isSupportLibJpeg() {
        String[] abis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            abis = Build.SUPPORTED_ABIS;
        } else {
            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }
        StringBuilder abiStr = new StringBuilder();
        for (String abi : abis) {
            abiStr.append(abi).append(",");
        }
        Log.i("LIBJPEG", abiStr.toString());
        if (abiStr.toString().contains("x86") || abiStr.toString().contains("x86_64") || abiStr.toString().contains
                ("mips") || abiStr.toString().contains("mips64") || abiStr.toString().contains("arm64-v8a")) {
            return false;
        } else {
            return true;
        }
    }

    public static int compressBitmap(Bitmap bit, int quality, String fileName, boolean isRecycle) {
        Bitmap result = null;
        try {
            result = bit.copy(Bitmap.Config.ARGB_8888, true);
            return compress(result, quality, fileName, true);
        } finally {
            BitmapUtils.recycleBitmaps(result);
            if (isRecycle) {
                BitmapUtils.recycleBitmaps(bit);
            }
        }

    }

    /**
     * 使用native方法进行图片压缩。
     * Bitmap的格式必须是ARGB_8888 {@link Bitmap.Config}。
     *
     * @param bitmap   图片数据
     * @param quality  压缩质量
     * @param dstFile  压缩后存放的路径
     * @param optimize 是否使用哈夫曼算法
     * @return 结果
     */
    public static native int compress(Bitmap bitmap, int quality, String dstFile, boolean optimize);
}
