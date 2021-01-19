package com.thkmon.blogparser.prototype;

import java.util.ArrayList;

import com.thkmon.blogparser.util.CheckUtil;

public class ObjList extends ArrayList<Object> {
	
	public void addNotEmpty(String str, String key) throws NullPointerException, Exception {
		if (CheckUtil.checkNullOrEmpty(str, key)) {
			super.add(str);
		}
	}

}