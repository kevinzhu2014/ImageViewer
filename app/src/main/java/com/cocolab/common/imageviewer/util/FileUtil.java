/**
 * @title: FileUtil.java
 * @author:Ray.Shi
 * @date 2014年4月24日
 */

package com.cocolab.common.imageviewer.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


public class FileUtil {

    static final String TAG = FileUtil.class.getName();

    // 拷贝
//	public static void copyDB(Context ctx, String dbName) {
//		final String dbFilename = App.DB_PATH + dbName;
//		try {
//			File dir = new File(App.DB_PATH);
//			// 判断是否已经存在
//			if (!dir.exists()) {
//				dir.mkdir();
//			}
//			InputStream assetsDB = ctx.getAssets().open(dbName);
//			FileOutputStream fos = new FileOutputStream(dbFilename);
//			byte[] buffer = new byte[1024 * 1024];
//			int count;
//			while ((count = assetsDB.read(buffer)) > 0) {
//				fos.write(buffer, 0, count);
//			}
//			fos.flush();
//			fos.close();
//			assetsDB.close();
//			Helper.showLog(TAG, "copy:" + dbName);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

    /*
     * 存储系统相机拍照的图片
     */
    public static File saveCameraImage(Bitmap btp) {
        String fileName = System.currentTimeMillis() + ".jpg";
        File tempFile = new File(SDcard.getImagesDir(), fileName);
        try {
            // 将bitmap转为jpg文件保存
            FileOutputStream fileOut = new FileOutputStream(tempFile);
            btp.compress(CompressFormat.JPEG, 100, fileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    /*
     * 保存temp.jpg文件
     */
    public static File saveTempFile(Bitmap btp) {
        File tempFile = SDcard.getTempImage();
        if (tempFile != null) {
            try {
                FileOutputStream fileOut = new FileOutputStream(tempFile);
                btp.compress(CompressFormat.JPEG, 100, fileOut);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return tempFile;
    }

    /*
     * 保存图片到手机Download文件夹下
     */
    public static File saveDownloadFile(Bitmap bmp) {
        String fileName = "ds" + System.currentTimeMillis() + ".jpg";
        return saveDownloadFile(bmp, fileName);
    }

    /*
     * 保存图片到手机Download文件夹下
     */
    public static File saveDownloadFile(Bitmap bmp, String fileName) {
        File tempFile = new File(SDcard.getDownloadDir(), fileName);
        if (bmp != null) {
            FileOutputStream fileOut = null;
            try {
                // 将bitmap转为jpg文件保存
                fileOut = new FileOutputStream(tempFile);
                bmp.compress(CompressFormat.JPEG, 100, fileOut);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fileOut != null) {
                    try {
                        fileOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return tempFile;
    }

    /*
     * 保存图片到手机Download文件夹下
     */
    public static File saveDownloadFile(byte[] bytes, String fileName) {
        File tempFile = new File(SDcard.getDownloadDir(), fileName);
        if (bytes != null) {
            FileOutputStream fileOut = null;
            try {
                // 将byte数组转为文件保存
                fileOut = new FileOutputStream(tempFile);
                fileOut.write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileOut != null) {
                    try {
                        fileOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return tempFile;
    }

    /*
     * 保存图片到手机DCIM/DS/文件夹下
     */
    public static File saveDsDCIMFile(Bitmap bmp) {
        String fileName = "ds" + System.currentTimeMillis() + ".jpg";
        return saveDsDCIMFile(bmp, fileName);
    }

    /*
     * 保存图片到手机DCIM/DS/文件夹下
     */
    public static File saveDsDCIMFile(Bitmap bmp, String fileName) {
        File tempFile = new File(SDcard.getAppDCIMDir(), fileName);
        try {
            // 将bitmap转为jpg文件保存
            FileOutputStream fileOut = new FileOutputStream(tempFile);
            bmp.compress(CompressFormat.JPEG, 100, fileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    /**
     * 使用当前时间戳拼接一个唯一的文件名
     *
     * @param format
     * @return
     */
    public static String getTempFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SS");
        String fileName = format.format(new Timestamp(System
                .currentTimeMillis()));
        return fileName;
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

    public static long getSize(File f) {
        if (f != null) {
            long size = 0;
            FileChannel fc = null;
            try {

                if (f.exists() && f.isFile()) {
                    FileInputStream fis = new FileInputStream(f);
                    fc = fis.getChannel();
                    size = fc.size();
                }
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            } finally {
                if (null != fc) {
                    try {
                        fc.close();
                    } catch (IOException e) {

                    }
                }
            }
            return size;
        }
        return 0;
    }

}
