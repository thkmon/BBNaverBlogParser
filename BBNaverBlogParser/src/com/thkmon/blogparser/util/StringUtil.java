package com.thkmon.blogparser.util;

public class StringUtil {

	public static int parseInt(String str, int defaultValue) {
		if (str == null) {
			return defaultValue;
		}

		int result = defaultValue;
		
		try {
			result = Integer.parseInt(str);
		} catch (Exception e) {
			result = defaultValue;
		}
		
		return result;
	}
}