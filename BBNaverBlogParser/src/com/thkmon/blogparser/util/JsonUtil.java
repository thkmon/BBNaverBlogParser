package com.thkmon.blogparser.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * org.json.JSONObject 라이브러리는 간편하게 json 파싱을 지원하나, 해당하는 요소가 없을 경우 여지없이 Exception
 * 을 발생시킨다. 따라서 불필요한 오류가 발생하지 않도록 본 유틸 클래스로 감싸서 사용.
 */
public class JsonUtil {

	/**
	 * 문자열을 json 객체로 파싱
	 *
	 * @param strJson
	 * @return
	 */
	public static JSONObject parseJsonObject(String strJson) {
		JSONObject result = null;
		try {
			result = new JSONObject(strJson);

		} catch (NullPointerException e) {
		} catch (Exception e) {
		}

		return result;
	}

	/**
	 * json 객체에서 특정키로 json 객체 가져오기
	 *
	 * @param jsonObj
	 * @param key
	 * @return
	 */
	public static JSONObject getJsonObject(JSONObject jsonObj, String key) {
		JSONObject result = null;
		try {
			result = jsonObj.getJSONObject(key);

		} catch (NullPointerException e) {
		} catch (Exception e) {
		}

		return result;
	}

	/**
	 * json 객체에서 특정키로 json 배열 가져오기
	 *
	 * @param jsonObj
	 * @param key
	 * @return
	 */
	public static JSONArray getJSONArray(JSONObject jsonObj, String key) {
		JSONArray result = null;
		try {
			result = jsonObj.getJSONArray(key);

		} catch (NullPointerException e) {
		} catch (Exception e) {
		}

		return result;
	}

	/**
	 * json 객체에서 특정키로 문자열 가져오기
	 *
	 * @param jsonObj
	 * @param key
	 * @return
	 */
	public static String getString(JSONObject jsonObj, String key) {
		String result = "";
		try {
			result = jsonObj.getString(key);
			if (result == null) {
				result = "";
			}

		} catch (NullPointerException e) {
		} catch (Exception e) {
		}

		return result;
	}
}