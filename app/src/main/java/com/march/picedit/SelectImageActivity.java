package com.march.picedit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.march.common.model.ImageInfo;
import com.march.gallery.GalleryActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * CreateAt : 2018/5/24
 * Describe :
 *
 * @author chendong
 */
public class SelectImageActivity extends GalleryActivity {

    public static final String KEY_DATA = "KEY_DATA";

    public static void startActivity(Activity context) {
        Intent intent = new Intent(context, SelectImageActivity.class);
        context.startActivityForResult(intent,100);
    }

    @Override
    protected void loadImg(Context context, String path, int width, int height, ImageView imageView) {
        if (path.endsWith("gif")) {
            Glide.with(context).load(path).into(imageView);
        } else {
            DrawableTypeRequest<String> load = Glide.with(context).load(path);
            if (width > 0 && height > 0) {
                load.override(width, height).into(imageView);
            } else {
                load.into(imageView);
            }
        }
    }

    @Override
    protected void onResult(List<ImageInfo> list) {
        Intent data = new Intent();
        ArrayList<ImageInfo> imageInfos = new ArrayList<>(list);
        data.putParcelableArrayListExtra(KEY_DATA, imageInfos);
        setResult(100, data);
        SelectImageActivity.this.finish();
    }
}
