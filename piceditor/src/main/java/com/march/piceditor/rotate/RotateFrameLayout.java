package com.march.piceditor.rotate;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.march.dev.utils.BitmapUtils;

/**
 * CreateAt : 7/18/17
 * Describe :
 *
 * @author chendong
 */
public class RotateFrameLayout extends FrameLayout {

    public RotateFrameLayout(@NonNull Context context) {
        super(context);
    }

    public RotateFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RotateFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static final int ANIM_DURATION = 200;
    public static final int FLIP_NO       = 1;
    public static final int FLIP_YES      = -1;
    public static final int ROTATE_LEVEL  = 90;

    private boolean mIsInAnim;

    public void flipX() {
        if (mIsInAnim)
            return;
        animate().scaleX(-getScaleX())
                .setDuration(ANIM_DURATION)
                .setInterpolator(new LinearInterpolator())
                .start();
    }

    public void flipY() {
        if (mIsInAnim)
            return;
        animate().scaleY(-getScaleY())
                .setDuration(ANIM_DURATION)
                .setInterpolator(new LinearInterpolator())
                .start();
    }

    public void rotateRight() {
        if (mIsInAnim)
            return;
        animate().rotationBy(ROTATE_LEVEL)
                .setDuration(ANIM_DURATION)
                .setListener(new MyAnimatorListener())
                .setInterpolator(new LinearInterpolator())
                .start();
    }


    public void rotateLeft() {
        if (mIsInAnim)
            return;
        animate().rotationBy(-ROTATE_LEVEL)
                .setDuration(ANIM_DURATION)
                .setListener(new MyAnimatorListener())
                .setInterpolator(new LinearInterpolator())
                .start();
    }

    public boolean isFlipX() {
        return getScaleX() == FLIP_YES;
    }

    public boolean isFlipY() {
        return getScaleY() == FLIP_YES;
    }

    public float getRotate() {
        return getRotation();
    }


    public class MyAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
            mIsInAnim = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mIsInAnim = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mIsInAnim = false;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    public void reset() {
        setScaleX(1);
        setScaleY(1);
        setRotation(0);
    }


    public void resetWithAnimation() {
        animate().scaleX(1).scaleY(1).rotation(0)
                .setDuration(ANIM_DURATION)
                .setListener(new MyAnimatorListener())
                .setInterpolator(new LinearInterpolator())
                .start();
    }


    public boolean isNotOperate() {
        return getScaleX() == 1 && getScaleY() == 1 && getRotation() % 360 == 0;
    }

    public Bitmap rotateBitmap(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        Matrix matrix = new Matrix();
        matrix.postScale(getScaleX(), getScaleY());
        matrix.postRotate(getRotate());
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        BitmapUtils.recycleBitmaps(bitmap);
        return newBitmap;
    }
}
