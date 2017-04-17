package com.cocolab.common.imageviewer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cocolab.common.imageviewer.logic.SmoothImageLogic;
import com.cocolab.common.imageviewer.logic.SystemBarTintManager;
import com.cocolab.common.imageviewer.util.FileUtil;
import com.cocolab.common.imageviewer.util.Helper;
import com.cocolab.common.imageviewer.util.Resolution;
import com.cocolab.common.imageviewer.util.SDcard;
import com.cocolab.common.imageviewer.view.SmoothGifImageView;
import com.cocolab.common.imageviewer.view.SmoothPhotoView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.droidsonroids.gif.GifDrawable;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageViewerActivity extends FragmentActivity implements OnClickListener {
    private MyViewPager viewPager;
    private MyPagerAdapter adapter;
    private TextView pageNumTV;
    private Button saveBtn;

    static private List<String> imgUrls;
    private boolean showDownload;
    private int index;
    private boolean hasTransformIn;
    private boolean hasTransformOut;

    private Map<String, Bitmap> bitmapMap = new HashMap<>();
    private Map<String, byte[]> gifBytesMap = new HashMap<>();
    private Bundle[] bundles;

    private OkHttpClient client;
    private Activity mActivity;

    private class MyPagerAdapter extends PagerAdapter {
        private List<String> iUrls;
        private View[] views;
        private Context context;

        public MyPagerAdapter(Context context, List<String> iUrls) {
            this.iUrls = iUrls;
            this.context = context;
            views = new View[iUrls.size()];//创建固定大小的集合
        }

        public View getItem(int position) {
            if (views != null && views.length > position) {
                return views[position];
            }
            return null;
        }

        @Override
        public int getCount() {
            return iUrls.size();
        }

        @Override
        public View instantiateItem(final ViewGroup container, final int position) {
            final String showImgURL = iUrls.get(position);
            int suffixIndex = showImgURL.lastIndexOf(".");
            boolean isGif = false;
            if (suffixIndex >= 0) {
                String suffixStr = showImgURL.substring(suffixIndex);
                if (suffixStr.equalsIgnoreCase(".gif")) {
                    isGif = true;
                }
            }
            if (isGif) {
                final SmoothGifImageView imgGIV = new SmoothGifImageView(context);
                String defUrl = null;
                //获取参数
                if (bundles != null && bundles.length > position) {
                    Bundle bundle = bundles[position];
                    if (bundle != null) {
                        //设置参数
                        int locationX = bundle.getInt("locationX");
                        int locationY = bundle.getInt("locationY");
                        int width = bundle.getInt("width");
                        int height = bundle.getInt("height");
                        defUrl = bundle.getString("def_url");
                        imgGIV.setOriginalInfo(width, height, locationX, locationY);
                    }
                }
                int drawableWidth = 0;
                int drawableHeight = 0;
                if (showImgURL.startsWith("http://") || showImgURL.startsWith("https://") || showImgURL.startsWith("ftp://")) {
                    boolean isCached = false;
                    try {
                        Iterator a = client.cache().urls();

                        while (a.hasNext()) {
                            if (a.next().equals(showImgURL)) {//存在缓存
                                isCached = true;
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (isCached) {
                        loadGifImageView(imgGIV, showImgURL, position, true);
                    } else {
                        if (defUrl != null && !"".equals(defUrl)) {
                            ImageLoader.getInstance().displayImage(defUrl, imgGIV, new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String imageUri, View view) {
                                    //如果选中的是当前图片，则显示动画
                                    if (index == position && !hasTransformIn) {
                                        hasTransformIn = true;
                                        imgGIV.transformIn(new SmoothGifImageView.TransformListener() {
                                            @Override
                                            public void onTransformComplete(int mode) {
                                                //2.小图显示完成后，再加载大图
                                                loadGifImageView(imgGIV, showImgURL, position, false);
                                            }
                                        });
                                    }

                                }

                                @Override
                                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                    loadGifImageView(imgGIV, showImgURL, position, false);
                                }

                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    //loadGifImageView(imgGIV, showImgURL, position, false);
                                }

                                @Override
                                public void onLoadingCancelled(String imageUri, View view) {
                                }
                            });
                        } else {
                            //无小图，直接动画加载大图
                            loadGifImageView(imgGIV, showImgURL, position, true);
                        }
                    }
                } else {
                    //本地文件，不做缓存
                    try {
                        Drawable drawable = new GifDrawable(showImgURL);
                        drawableWidth = drawable.getIntrinsicWidth();
                        drawableHeight = drawable.getIntrinsicHeight();
                        if (drawableWidth > 0 && drawableHeight > 0) {
                            ViewPager.LayoutParams lps = (ViewPager.LayoutParams) imgGIV.getLayoutParams();
                            if (lps == null) {
                                lps = new ViewPager.LayoutParams();
                            }
                            lps.width = Resolution.getScreenWidth(mActivity);
                            lps.height = lps.width * drawableHeight / drawableWidth;
                        }
                        imgGIV.setImageDrawable(drawable);
                        if (index == position && !hasTransformIn) {
                            imgGIV.transformIn();//播放动画
                            hasTransformIn = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        imgGIV.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.global_pic_bg));
                        if (index == position && !hasTransformIn) {
                            imgGIV.transformIn();//播放动画
                            hasTransformIn = true;
                        }
                    }
                }

                imgGIV.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pageNumTV.setVisibility(View.INVISIBLE);
                        //设置viewpager的背景为透明
                        //viewPager.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                        if (!hasTransformOut) {
                            imgGIV.transformOut();
                            hasTransformOut = true;
                        }
                    }
                });
                imgGIV.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //showSavePictureDialog(position);
                        return true;
                    }
                });

                imgGIV.setOnTransformListener(new SmoothGifImageView.TransformListener() {
                    @Override
                    public void onTransformComplete(int mode) {
                        if (mode == SmoothPhotoView.STATE_TRANSFORM_IN) {
                            if (imgUrls.size() > 1) {
                                pageNumTV.setVisibility(View.VISIBLE);
                            } else {
                                pageNumTV.setVisibility(View.INVISIBLE);
                            }
                            //设置vierpager的背景为黑色
                            //viewPager.setBackgroundColor(context.getResources().getColor(R.color.black));
                        } else if (mode == SmoothPhotoView.STATE_TRANSFORM_OUT) {
                            finish();
                        }
                    }
                });

                container.addView(imgGIV);
                views[position] = imgGIV;

                return imgGIV;
            } else {
                final SmoothPhotoView photoView = new SmoothPhotoView(container.getContext());
                String defUrl = null;
                //获取参数
                if (bundles != null && bundles.length > position) {
                    Bundle bundle = bundles[position];
                    if (bundle != null) {
                        //设置参数
                        int locationX = bundle.getInt("locationX");
                        int locationY = bundle.getInt("locationY");
                        int width = bundle.getInt("width");
                        int height = bundle.getInt("height");
                        defUrl = bundle.getString("def_url");

                        photoView.setOriginalInfo(width, height, locationX, locationY);
                    }
                }
                //先判断大图是否已经缓存
                boolean isCached = false;
                try {
                    File cacheFile = ImageLoader.getInstance().getDiskCache().get(showImgURL);
                    if (cacheFile != null && cacheFile.exists()) {
                        isCached = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (isCached) {
                    //大图已经缓存
                    loadPhotoView(photoView, showImgURL, position, true);
                } else {
                    //大图未缓存
                    if (defUrl != null && !"".equals(defUrl)) {
                        //1.动画显示小图
                        ImageLoader.getInstance().displayImage(defUrl, photoView, new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {
                                //如果选中的是当前图片，则显示动画
                                if (index == position && !hasTransformIn) {
                                    hasTransformIn = true;
                                    photoView.transformIn(new SmoothPhotoView.TransformListener() {
                                        @Override
                                        public void onTransformComplete(int mode) {
                                            //2.小图显示完成后，再加载大图
                                            loadPhotoView(photoView, showImgURL, position, false);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                loadPhotoView(photoView, showImgURL, position, false);
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                //2.小图显示完成后，再加载大图
                                //loadPhotoView(photoView, showImgURL, position, false);
                            }

                            @Override
                            public void onLoadingCancelled(String imageUri, View view) {
                            }
                        });
                    } else {
                        //无小图，直接动画加载大图
                        loadPhotoView(photoView, showImgURL, position, true);
                    }
                }
                photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                    @Override
                    public void onPhotoTap(View view, float x, float y) {
                        pageNumTV.setVisibility(View.INVISIBLE);
                        //设置viewpager的背景为透明
                        //viewPager.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                        if (!hasTransformOut) {
                            photoView.transformOut();
                            hasTransformOut = true;
                        }
                    }

                    @Override
                    public void onOutsidePhotoTap() {

                    }
                });
                photoView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //showSavePictureDialog(position);
                        return true;
                    }
                });
                photoView.setOnTransformListener(new SmoothPhotoView.TransformListener() {
                    @Override
                    public void onTransformComplete(int mode) {
                        if (mode == SmoothPhotoView.STATE_TRANSFORM_IN) {
                            if (imgUrls.size() > 1) {
                                pageNumTV.setVisibility(View.VISIBLE);
                            } else {
                                pageNumTV.setVisibility(View.INVISIBLE);
                            }
                            //设置vierpager的背景为黑色
                            //viewPager.setBackgroundColor(context.getResources().getColor(R.color.black));
                        } else if (mode == SmoothPhotoView.STATE_TRANSFORM_OUT) {
                            finish();
                        }
                    }
                });

                container.addView(photoView);
                views[position] = photoView;

                return photoView;
            }

            //return container;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            views[position] = null;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public void loadGifImageView(final SmoothGifImageView imgGIV, final String showImgURL, final int position, final boolean transformIn) {
            Request request = new Request.Builder()
                    .header("Cache-Control", "max-stale=" + 60 * 60 * 24 * 28)
                    .url(showImgURL)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Drawable drawable = null;
                    int drawableWidth = 0;
                    int drawableHeight = 0;
                    boolean success = false;
                    byte[] gifBytes = response.body().bytes();
                    try {
                        //尝试转成GIF
                        drawable = new GifDrawable(gifBytes);
                        drawableWidth = drawable.getIntrinsicWidth();
                        drawableHeight = drawable.getIntrinsicHeight();

                        success = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        //失败则转成Bitmap
                        ByteArrayInputStream bais = new ByteArrayInputStream(gifBytes);
                        drawable = new BitmapDrawable(context.getResources(), bais);
                        drawableWidth = drawable.getIntrinsicWidth();
                        drawableHeight = drawable.getIntrinsicHeight();

                        bais.close();

                        success = true;
                    }
                    if (success) {
                        gifBytesMap.put(showImgURL, gifBytes);//添加在Map中（GIF)

                        final int finalDrawableWidth = drawableWidth;
                        final int finalDrawableHeight = drawableHeight;
                        final Drawable finalDrawable = drawable;
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalDrawableWidth > 0 && finalDrawableHeight > 0) {
                                    ViewPager.LayoutParams lps = (ViewPager.LayoutParams) imgGIV.getLayoutParams();
                                    if (lps == null) {
                                        lps = new ViewPager.LayoutParams();
                                    }
                                    lps.width = Resolution.getScreenWidth(mActivity);
                                    lps.height = lps.width * finalDrawableHeight / finalDrawableWidth;
                                    imgGIV.setLayoutParams(lps);
                                }
                                imgGIV.setImageDrawable(finalDrawable);
                                if (index == position && transformIn && !hasTransformIn) {
                                    imgGIV.transformIn();//播放动画
                                    hasTransformIn = true;
                                }
                            }
                        });

                    } else {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imgGIV.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.global_pic_bg));
                                if (index == position && transformIn && !hasTransformIn) {
                                    imgGIV.transformIn();//播放动画
                                    hasTransformIn = true;
                                }
                            }
                        });
                    }
                }
            });
        }
        public void loadPhotoView(final SmoothPhotoView photoView, final String showImgURL, final int position, final boolean transformIn) {
            Target mTarget = new Target() {
                //图片加载成功
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    //如果选中的是当前图片，则显示动画
                    if (index == position && transformIn && !hasTransformIn) {
                        photoView.transformIn();
                        hasTransformIn = true;
                    }
                    photoView.setImageBitmap(bitmap);
                    bitmapMap.put(showImgURL, bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    //如果选中的是当前图片，则显示动画
                    /*if (index == position && transformIn && !hasTransformIn) {
                        photoView.transformIn();
                        hasTransformIn = true;
                    }*/
                }
            };
            Picasso.with(context).load(showImgURL).into(mTarget);
        }

        public void loadPhotoView2(final SmoothPhotoView photoView, final String showImgURL, final int position, final boolean transformIn) {
            ImageLoader.getInstance().displayImage(showImgURL, photoView, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build(),
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            //如果选中的是当前图片，则显示动画
                            if (index == position && transformIn && !hasTransformIn) {
                                photoView.transformIn();
                                hasTransformIn = true;
                            }
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            /*if (bitmapMap.get(showImgURL) != null) {
                                bitmapMap.get(showImgURL).recycle();//先回收，再添加
                            }*/
                            bitmapMap.put(showImgURL, loadedImage);

                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                        }
                    });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.image_viewer_activity);
        setSystemBarTint();
        //设置缓存，缓存到SD卡工程的cache目录下
        int cacheSize = 100 * 1024 * 1024;//100M
        Cache cache = new Cache(SDcard.getCacheDir(), cacheSize);
        client = new OkHttpClient.Builder()
                .cache(cache)
                .build();
        imgUrls = new ArrayList<>();
        List<String> tempUrls = this.getIntent().getStringArrayListExtra("img_urls");

        if (tempUrls != null) {
            for (String url : tempUrls) {
                boolean needFixUrl = true;
                if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://")
                                || url.startsWith("drawable://") || url.startsWith("content://")
                                || url.startsWith("assets://")) {
                            needFixUrl = false;
                }
                if(needFixUrl){
                    url = "file://" + url;
                }
                imgUrls.add(url);
            }
        }

        index = this.getIntent().getIntExtra("index", 0);
        showDownload = this.getIntent().getBooleanExtra("show_download", true);
        //设置图片位置参数
        Parcelable[] parcelables = this.getIntent().getParcelableArrayExtra(SmoothImageLogic.KEY_BUNDLE_IMAGE_PARAMS);
        bundles = new Bundle[parcelables.length];
        for (int i = 0; i < parcelables.length; i++) {
            bundles[i] = (Bundle) parcelables[i];
        }

        stupViews();

        if (imgUrls != null) {
            /*if (imgUrls.size() > 1) {
                pageNumTV.setVisibility(View.VISIBLE);
            } else {
                pageNumTV.setVisibility(View.GONE);
            }*/
            pageNumTV.setVisibility(View.INVISIBLE);

            adapter = new MyPagerAdapter(mActivity, imgUrls);

            //viewPager.setOffscreenPageLimit(imgUrls.size());
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(index);
            if (imgUrls.size() > 1) {
                pageNumTV.setText((index + 1) + "/" + imgUrls.size());
            }
        } else {
            pageNumTV.setVisibility(View.INVISIBLE);
        }
    }

    private void setSystemBarTint() {
        //设置沉浸的颜色
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(mActivity.getResources().getColor(android.R.color.transparent));
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            container.setFitsSystemWindows(true);
        }*/
    }

    private void stupViews() {
        pageNumTV = (TextView) findViewById(R.id.page_num_tv);
        saveBtn = (Button) findViewById(R.id.image_save_btn);
        viewPager = (MyViewPager) findViewById(R.id.global_viewpager);
        viewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                if (imgUrls.size() > 1) {
                    pageNumTV.setText((arg0 + 1) + "/" + imgUrls.size());
                }
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
        });
        saveBtn.setVisibility(View.GONE);
    }

    public void saveImageToLocal(int position) {
        String imgURL = imgUrls.get(position);
        if (imgURL != null && (!imgURL.startsWith("http://") && !imgURL.startsWith("https://") || imgURL.startsWith("ftp://"))) {
            //已经是本地图片了，不需要下载
            Helper.showToast(mActivity, "图片已经下载" + imgURL);
            return;
        }
        int suffixIndex = imgURL.lastIndexOf(".");
        String suffixStr = ".jpg";
        if(suffixIndex >= 0) {
            suffixStr = imgURL.substring(suffixIndex);
        }
        if (suffixStr.equalsIgnoreCase(".gif")) {
            byte[] bytes = gifBytesMap.get(imgURL);
            if (bytes == null) {
                Helper.showToast(mActivity, "已经是本地图片，或者正在加载中");
                return;
            } else {
                String fileName = FileUtil.getTempFileName() + ".gif";
                File savedFile = FileUtil.saveDownloadFile(bytes, fileName);
                if (savedFile.exists()) {
                    FileUtil.scanSingleFile(mActivity, savedFile.getAbsolutePath());
                    Helper.showToast(mActivity,
                            "图片已经保存到本地" + savedFile.getAbsolutePath());
                }
            }

        } else {
            Bitmap bitmap = bitmapMap.get(imgURL);
            if (bitmap == null) {
                Helper.showToast(mActivity, "已经是本地图片，或者正在加载中");
                return;
            } else {
                String fileName = FileUtil.getTempFileName() + ".jpg";
                File savedFile = FileUtil.saveDownloadFile(bitmap, fileName);
                if (savedFile.exists()) {
                    FileUtil.scanSingleFile(mActivity, savedFile.getAbsolutePath());
                    Helper.showToast(mActivity,
                            "图片已经保存到本地" + savedFile.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        pageNumTV.setVisibility(View.INVISIBLE);
        //设置vierpager的背景为透明
        //viewPager.setBackgroundColor(activity.getResources().getColor(R.color.transparent));

        int position = viewPager.getCurrentItem();
        View view = adapter.getItem(position);
        if (view != null) {
            if (view instanceof SmoothPhotoView) {
                if (!hasTransformOut) {
                    ((SmoothPhotoView) view).transformOut();
                    hasTransformOut = true;
                }
            } else if (view instanceof SmoothGifImageView) {
                if (!hasTransformOut) {
                    ((SmoothGifImageView) view).transformOut();
                    hasTransformOut = true;
                }
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
