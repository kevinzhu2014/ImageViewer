package com.cocolab.common.imageviewer.logic;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import com.cocolab.common.imageviewer.util.ImageUtil;
import com.cocolab.common.imageviewer.util.Resolution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhushengui on 2016/6/28.
 */
public class SmoothImageLogic {
    public static final String KEY_BUNDLE_IMAGE_PARAMS = "image_params";
    private Map<String, View> urlViewMap = new HashMap<String, View>();

    public void addViewLocationListener(final String url, final View iv){
        iv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                urlViewMap.put(url, iv);
            }
        });
    }

    public Bundle[] getViewParams(Context ctx, List<String> imgUrls, int imgType){
        Bundle[] bundles = new Bundle[imgUrls.size()];
        for(int i = 0 ; i < imgUrls.size() ; i++){
            String url = imgUrls.get(i);
            bundles[i] = getViewParam(ctx, urlViewMap.get(url));
            bundles[i].putString("def_url", ImageUtil.getSmallImageUrl(ctx, url, imgType));
        }
        return bundles;
    }

    public Bundle[] getViewParams(Context ctx, View iv, String imgUrl, int imgType){
        Bundle[] bundles = new Bundle[1];
        bundles[0] = getViewParam(ctx, iv);
        bundles[0].putString("def_url", imgType == -1 ? imgUrl : ImageUtil.getSmallImageUrl(ctx, imgUrl, imgType));

        return bundles;
    }

    public Bundle getViewParam(Context context, View iv){
        Bundle bundle = new Bundle();
        int[] location = new int[2];
        if(iv == null || iv.getWidth() == 0){
            bundle.putInt("locationX", Resolution.getScreenWidth(context) / 2);//必须
            bundle.putInt("locationY", Resolution.getScreenHeight(context) / 2);//必须

            bundle.putInt("width", 1);//必须
            bundle.putInt("height", 1);//必须
        }else {
            iv.getLocationOnScreen(location);
            bundle.putInt("locationX", location[0]);//必须
            bundle.putInt("locationY", location[1]);//必须

            bundle.putInt("width", iv.getWidth());//必须
            bundle.putInt("height", iv.getHeight());//必须
        }
        return bundle;
    }
}
