package com.thkmon.blogparser.util;

import java.io.File;

public class FolderUtil {
	/**
	 * 특정 파일패스의 부모 폴더가 없을 경우 만든다.
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean makeParentDir(String filePath) {

		if (filePath == null || filePath.trim().length() == 0) {
			System.err.println("makeParentDir : filePath == null || filePath.length() == 0");
			return false;

		} else {
			filePath = filePath.trim();
		}

		if (filePath.indexOf("/") > -1) {
			filePath = filePath.replace("/", "\\");
		}

		while (filePath.indexOf("\\\\") > -1) {
			filePath = filePath.replace("\\\\", "\\");
		}

		// 필요한 디렉토리 만들기
		int lastSlashPos = filePath.lastIndexOf("\\");

		if (lastSlashPos > -1) {
			File d = new File(filePath.substring(0, lastSlashPos));
			if (!d.exists()) {
				d.mkdirs();
			}

		} else {
			System.err.println("makeParentDir : lastSlashPos not exists");
			return false;
		}

		return true;
	}
	
	public static boolean deleteFolder(String folderPath) {
		File folder = new File(folderPath);
		return deleteFolder(folder);
	}
	
	
	private static boolean deleteFolder(File obj) {
		boolean result = true;
		boolean oneResult = true;
		
		if (obj == null || !obj.exists()) {
			return false;
		}
		
		if (obj.isDirectory()) {
			File[] fileArr = obj.listFiles();
			if (fileArr != null && fileArr.length > 0) {
				int fileCount = fileArr.length;
				for (int i=0; i<fileCount; i++) {
					oneResult = deleteFolder(fileArr[i]);
					if (!oneResult) {
						result = false;
					}
				}
			}
			
			if (result) {
				oneResult = obj.delete();
				if (!oneResult) {
					result = false;
				}
			}
			
		} else if (obj.isFile()) {
			return obj.delete();
		}
		
		return result;
	}
}