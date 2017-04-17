/**
@title: Resolution.java
@author:Ray.Shi
@date 2014年4月24日
 */

package com.cocolab.common.imageviewer.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.TypedValue;
import android.view.View;

import java.lang.reflect.Field;

public class Resolution {

	static final String TAG = Resolution.class.getSimpleName();

	public static int getHeight(Context context, String height, String width) {
		int w = Integer.parseInt(width);
		int h = Integer.parseInt(height);
		return getHeight(context, w, h);
	}

	public static int getHeight(Context context, int width, int height) {
		int screenWidth = getScreenWidth(context);
		return getRateHeight(context, screenWidth, width, height);
	}

	/**
	 * @param getHeight
	 *            context,width：当前图片宽度，originWidth：原始图片宽度，originHeight：原始图片宽高度
	 */
	public static int getRateHeight(Context context, int width, int originWidth, int originHeight) {
		int h = originHeight * width / originWidth;
		return h;
	}

	/**
	 * @param getHeight
	 *            context,height：当前图片高度，originWidth：原始图片宽度，originHeight：原始图片宽高度
	 */
	public static int getRateWidth(Context context, int height, int originWidth, int originHeight) {
		int w = originWidth * height / originHeight;
		return w;
	}

	public static int getWidthBasedHeight(int width, int height, int baseHieght) {
		return width / height * baseHieght;
	}

	/*
	 * 得到屏幕的宽度
	 */
	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	/*
	 * 得到屏幕的高度
	 */
	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	/*
	 * 压缩图片尺寸
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width, (int) height, matrix, true);
		return bitmap;
	}

	/*
	 * 截屏
	 */
	public static Bitmap getViewBitmap(View v) {
		View view = v.getRootView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		return bitmap;
	}

	/*
	 * 获取状态栏高度
	 */

	public static int getActionBarHeight(Context ctx) {
		TypedValue tv = new TypedValue();
		if (ctx.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
			return TypedValue.complexToDimensionPixelSize(tv.data, ctx.getResources().getDisplayMetrics());
		else
			return 0;
	}

	public static int getStatusBarHeight(Context ctx){
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = ctx.getResources().getDimensionPixelSize(x);
		} catch(Exception e1) {
			e1.printStackTrace();
		}
		return sbar;
	}

	/*
	 * 裁剪图片 裁剪顶部尺寸 0切顶部，1切底部
	 */
	public static Bitmap cutBitmap(Bitmap bmp, int cut, int pos) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap;
		if (pos == 0)
			bitmap = Bitmap.createBitmap(bmp, 0, cut, width, height - cut, new Matrix(), true);
		else
			bitmap = Bitmap.createBitmap(bmp, 0, 0, width, height - cut, new Matrix(), true);
		return bitmap;
	}

	public static int getRowCount(Context ctx) {
		if (getScreenHeight(ctx) < 801)
			return 2;
		else
			return 3;
	}

}
