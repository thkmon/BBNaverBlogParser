package com.thkmon.blogparser.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * org.json.JSONObject ���̺귯���� �����ϰ� json �Ľ��� �����ϳ�, �ش��ϴ� ��Ұ� ���� ��� �������� Exception
 * �� �߻���Ų��. ���� ���ʿ��� ������ �߻����� �ʵ��� �� ��ƿ Ŭ������ ���μ� ���.
 */
public class JsonUtil {

	/**
	 * ���ڿ��� json ��ü�� �Ľ�
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
	 * json ��ü���� Ư��Ű�� json ��ü ��������
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
	 * json ��ü���� Ư��Ű�� json �迭 ��������
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
	 * json ��ü���� Ư��Ű�� ���ڿ� ��������
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