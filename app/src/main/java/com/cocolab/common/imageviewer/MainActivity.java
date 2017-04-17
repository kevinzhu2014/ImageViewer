package com.cocolab.common.imageviewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.cocolab.common.imageviewer.logic.SmoothImageLogic;
import com.cocolab.common.imageviewer.util.ImageLoaderUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //这里放入一个测试的项目
        final String url1 = "https://ss3.baidu.com/9fo3dSag_xI4khGko9WTAnF6hhy/image/h%3D360/sign=026b99c291cad1c8cfbbfa214f3f67c4/83025aafa40f4bfbc9c817b9074f78f0f63618c6.jpg";
        final String url2 = "http://ww2.sinaimg.cn/large/85cccab3gw1etdjcto7ofg209z07ykf2.gif";

        final ImageView img1 = (ImageView) this.findViewById(R.id.img1);
        final ImageView img2 = (ImageView) this.findViewById(R.id.img2);
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> urls = new ArrayList<String>();
                urls.add(url1);
                urls.add(url2);
                showBigPic(new View[]{img1, img2}, urls);
            }
        });
        ImageLoaderUtil.initImageLoader(this.getApplicationContext());
        ImageLoader.getInstance().displayImage(url1, img1);

        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> urls = new ArrayList<String>();
                urls.add(url2);
                showBigPic(new View[]{img2}, urls);
            }
        });
        ImageLoader.getInstance().displayImage(url2, img2);
    }

    private void showBigPic(View[] views, ArrayList<String> imgUrls){
        SmoothImageLogic logic = new SmoothImageLogic();
        Bundle[] bundles = new Bundle[views.length];
        for(int i = 0 ; i < views.length ; i++) {
            bundles[i] = logic.getViewParam(this, views[i]);
        }
        Intent intent = new Intent(this, ImageViewerActivity.class)
                .putStringArrayListExtra("img_urls", imgUrls)
                .putExtra(SmoothImageLogic.KEY_BUNDLE_IMAGE_PARAMS, bundles);

        this.startActivity(intent);
    }
}
