package com.cocolab.common.imageviewer.util;

import android.content.Context;

public class ImageUtil {
	public static final long IMG_SIZE_1_MB =  1 * 1024 * 1024;// 单位为B
	public static final long IMG_SIZE_512_KB =  512 * 1024;// 单位为B
	public static final long IMG_SIZE_256_KB =  256 * 1024;// 单位为B
	public static final long IMG_SIZE_128_KB =  128 * 1024;// 单位为B

	public static String getSmallImageUrl(String url, int type){
		String smallImageUrl = null;
		try{
			int lastIndex = url.lastIndexOf(".");
			smallImageUrl = url.substring(0, lastIndex) + "_" + type + url.substring(lastIndex);
		}catch(Exception e){
			e.printStackTrace();
		}
		return smallImageUrl == null ? url : smallImageUrl;
	}

	public static String getSmallImageUrl(Context ctx, String url, int type){
		return url;
	}
}
