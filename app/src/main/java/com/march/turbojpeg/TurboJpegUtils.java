package com.march.turbojpeg;

import android.graphics.Bitmap;
import android.os.Build;

import com.march.dev.utils.BitmapUtils;

import java.io.File;

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


    public static void checkSupportArmeabi() {
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
        isSupportArmeabi = abiStr.toString().contains("armeabi") || abiStr.toString().contains("armeabi-v7a");
    }

    public static int compressBitmap(Bitmap bit, int quality, String fileName, boolean isRecycle) {
        checkSupportArmeabi();
        Bitmap result = null;
        if (isSupportArmeabi) {
            try {
                File file = new File(fileName);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (!file.exists()) {
                    file.createNewFile();
                }
                result = bit.copy(Bitmap.Config.ARGB_8888, true);
                return compress(result, quality, fileName, true);
            } catch (Exception e) {
                e.printStackTrace();
                BitmapUtils.compressImage(bit, new File(fileName), Bitmap.CompressFormat.JPEG, 100, isRecycle);
                return 1;
            } finally {
                BitmapUtils.recycleBitmaps(result);
                if (isRecycle) {
                    BitmapUtils.recycleBitmaps(bit);
                }
            }
        } else {
            BitmapUtils.compressImage(bit, new File(fileName), Bitmap.CompressFormat.JPEG, 100, isRecycle);
            return 1;
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
