/**
@title: SDcard.java
@author:Ray.Shi
@date 2014年4月24日
 */

package com.cocolab.common.imageviewer.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SDcard {

	static final String TAG = SDcard.class.getSimpleName();

	public static final String APP_DIR = "imageviewer";
	public static final String IMAGES_DIR = "images";
	public static final String CACHE_DIR = "cache";
	public static final String DOWNLOAD_DIR = "download";
	public static final String GUIDE_DIR = "guide";
	public static final String DCIM_DIR ="DCIM";
	public static final String TEMP_IMAGE = "temp.jpg";

	public static File getAppDir() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File appDir = new File(Environment.getExternalStorageDirectory(), APP_DIR);
			if (!appDir.exists()) {
				appDir.mkdir();
			}
			return appDir;
		}
		return null;
	}

	public static File getImagesDir() {
		File appDir = getAppDir();
		if (appDir != null) {
			File imagesDir = new File(appDir, IMAGES_DIR);
			if (!imagesDir.exists()) {
				imagesDir.mkdir();
			}
			return imagesDir;
		}
		return null;
	}

	public static File getGuideDir() {
		File appDir = getAppDir();
		if (appDir != null) {
			File guideDir = new File(appDir, GUIDE_DIR);
			if (!guideDir.exists()) {
				guideDir.mkdir();
			}
			return guideDir;
		}
		return null;
	}

	public static File getCacheDir() {
		File appDir = getAppDir();
		if (appDir != null) {
			File cacheDir = new File(appDir, CACHE_DIR);
			if (!cacheDir.exists()) {
				cacheDir.mkdir();
			}
			return cacheDir;
		}
		return null;
	}

	public static File getTempImage() {
		File appDir = getAppDir();
		if (appDir != null) {
			File tempImage = new File(appDir, TEMP_IMAGE);
			if (!tempImage.exists()) {
				try {
					tempImage.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return tempImage;
		}
		return null;
	}

	/**
	 * 使用当前时间戳拼接一个唯一的文件名
	 * 
	 * @param format
	 * @return
	 */
	public static String getTempFileName() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SS");
		String fileName = format.format(new Date());
		return fileName;
	}
	public static File getDownloadDir() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File dir = new File(Environment.getExternalStorageDirectory(), DOWNLOAD_DIR);
			if (!dir.exists()) {
				dir.mkdir();
			}
			return dir;
		}
		return null;
	}
	
	/**
	 * 扫描
	 * 
	 * @param filePath
	 */
	public static void scanSingleFile(Context context, String filePath) {
		if (filePath != null && "is-fuck-null".equals(filePath))
			return;
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.parse("file://" + filePath);
		intent.setData(uri);
		context.sendBroadcast(intent);

	}
	public static File getAppDCIMDir() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File dcimDir = new File(Environment.getExternalStorageDirectory(), DCIM_DIR);
			if (!dcimDir.exists()) {
				dcimDir.mkdir();
			}
			File dsDir =new File(dcimDir, APP_DIR);
			if (!dsDir.exists()) {
				dsDir.mkdir();
			}
			return dsDir;
		}
		return null;
	}

}
