package com.thkmon.blogparser.util;

public class CheckUtil {

	public static boolean checkNullOrEmpty(String str, String key) throws NullPointerException, Exception {
		if (key == null || key == "") {
			key = "unknown";
		}
		
		if (str == null || str.length() == 0) {
			throw new NullPointerException("CheckUtil checkNullOrEmpty : ERROR [" + key + "]");
		}
		
		return true;
	}
}
