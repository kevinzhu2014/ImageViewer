package com.cocolab.common.imageviewer.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Helper {

	public static final boolean DEBUG = false;

	public static void showToast(Context ctx, CharSequence text) {
		Toast.makeText(ctx.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}

	public static void showLong(Context ctx, CharSequence text) {
		Toast.makeText(ctx.getApplicationContext(), text, Toast.LENGTH_LONG).show();
	}

	public static void showLong(Context ctx, int resId) {
		Toast.makeText(ctx.getApplicationContext(), resId, Toast.LENGTH_LONG).show();
	}

	public static void showToast(Context ctx, int resId) {
		Toast.makeText(ctx.getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
	}

	public static void showLog(String tag, String text) {
		if (DEBUG){
			Log.e(tag, text == null ? "is null" : text);
		}
	}

	public static void showLog(String tag, String text, Throwable tr) {
		if (DEBUG){
			Log.e(tag, text == null ? "is null" : text, tr);
		}
	}
}
