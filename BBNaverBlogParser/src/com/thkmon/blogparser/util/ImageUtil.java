package com.thkmon.blogparser.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class ImageUtil {
	public static boolean downloadImgFromUrl(String strUrl, String filePathToSave) {
        if (strUrl == null || strUrl.length() == 0) {
            return false;
        }
        
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        
        try {
            inputStream = new URL(strUrl).openStream();
            
            File file = new File(filePathToSave);
            FolderUtil.makeParentDir(file.getAbsolutePath());
            
            outputStream = new FileOutputStream(file, false);
            
            int length;
            byte[] buffer = new byte[12288]; // 12K
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (NullPointerException e) {
            } catch (Exception e) {
            } finally {
            	outputStream = null;
            }
            
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (NullPointerException e) {
            } catch (Exception e) {
            } finally {
            	inputStream = null;
            }
        }
        
        File file = new File(filePathToSave);
        return file.exists() && file.length() > 0;
    }
}