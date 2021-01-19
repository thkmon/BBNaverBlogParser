package com.thkmon.blogparser.prototype;

import java.util.HashMap;
import java.util.Iterator;

public class BasicMap extends HashMap<String, Object> {
	
	private StringList BlackListKey = null;
	
	public Object put(String key, Object obj) {
		return super.put(key.toLowerCase(), obj);
	}
	
	public void setString(String key, String strValue) {
		super.put(key.toLowerCase(), strValue);
	}
	
	public Object get(String key) {
		key = key.toLowerCase();
        return super.get(key);
    }
	
	public String getKey(int index) {
		if (index < 0) {
			return "";
		}
		
		Iterator iter = super.keySet().iterator();
		Object obj = null;
		
		int curIndex = 0;
		while (iter.hasNext()) {
			obj = iter.next();
			if (curIndex == index) {
				if (obj != null) {
					return String.valueOf(obj).toLowerCase();
				} else {
					return "";
				}
			}
			
			curIndex++;
		}
		
		return "";
    }
	
	public String getString(String key, String defaultString) {
		try {
			return String.valueOf(this.get(key.toLowerCase()));
		} catch (Exception e) {
			return defaultString;
		}
    }
	
	public int getInt(String key, int defaultInt) {
		try {
			return Integer.parseInt(getString(key, String.valueOf(defaultInt)));
		} catch (Exception e) {
			return defaultInt;
		}
    }
	
	public String getString(int index, String defaultString) {
		try {
			return getString(getKey(index), defaultString);
		} catch (Exception e) {
			return defaultString;
		}
    }
	
	public int getInt(int index, int defaultInt) {
		try {
			return getInt(getKey(index), defaultInt);
		} catch (Exception e) {
			return defaultInt;
		}
    }
	
//	public String getNotEmptyString(String key) throws Exception {
//		try {
//			key = key.toLowerCase();
//			
//			Object obj = super.get(key);
//			if (obj == null) {
//				throw new Exception("getNotNullString : value is null. key is [" + key + "]");
//			}
//			
//			String val = String.valueOf(obj);
//			if (val.trim().length() == 0) {
//				throw new Exception("getNotNullString : value is empty. key is [" + key + "]");
//			}
//			
//			return val;
//			
//		} catch (Exception e) {
//			throw e;
//		}
//    }
	
	/**
	 * �썝�븯�뒗 key�뱾�쓣 ���긽�쑝濡� key-value 媛� 異쒕젰�릺�뒗 json�쓣 留뚮뱾�뼱�꽌 由ы꽩�븳�떎.
	 * 
	 * @param keys
	 * @return
	 */
	public String toJson(String... keys) {
		if (keys == null || keys.length == 0) {
			return "{}";
		}
		
		StringBuffer content = new StringBuffer();
		
		content.append("{");
		
		String oneKey = "";
		
		int keyCount = keys.length;
		for (int i=0; i<keyCount; i++) {
			
			oneKey = keys[i];
			
			if (oneKey == null || oneKey.trim().length() == 0) {
				continue;
				
			} else {
				oneKey = oneKey.trim();
			}
			
			content.append("\"");
			content.append(oneKey.replace("\"", ""));
			content.append("\"");
			content.append(" : ");
			content.append("\"");
			content.append(this.get(oneKey).toString().replace("\"", ""));
			content.append("\"");
			content.append(", ");
		}
		
		int lastComma = content.lastIndexOf(", ");
		content.deleteCharAt(lastComma);
		content.deleteCharAt(lastComma);
		
		content.append("}");
		
		return content.toString();
		
	}
	
	/**
	 * 紐⑤뱺 key瑜� ���긽�쑝濡� key-value 媛� 異쒕젰�릺�뒗 json�쓣 留뚮뱾�뼱�꽌 由ы꽩�븳�떎.
	 * �떒, 釉붾옓由ъ뒪�듃�뒗 �젣�쇅�맂�떎.
	 * 
	 * @return
	 */
	public String toJson() {
		if (this.keySet() == null) {
			return "{}";
		}
		
		StringBuffer content = new StringBuffer();
		
		content.append("{");
		
		String oneKey = "";
		
		Iterator<String> iter = keySet().iterator();
		while (iter.hasNext()) {
			oneKey = iter.next();
			if (oneKey == null || oneKey.length() == 0) {
				continue;
			}
			
			boolean isBlackList = false;
			
			if (BlackListKey != null && BlackListKey.size() > 0) {
				for(int k=0; k<BlackListKey.size(); k++) {
					if (BlackListKey.get(k).equals(oneKey)) {
						isBlackList = true;
						break;
					}
				}
			}
			
			if (isBlackList) {
				continue;
			}
			
			content.append("\"");
			content.append(oneKey.replace("\"", ""));
			content.append("\"");
			content.append(" : ");
			content.append("\"");
			content.append(this.get(oneKey).toString().replace("\"", ""));
			content.append("\"");
			content.append(", ");
		}
		
		int lastComma = content.lastIndexOf(", ");
		content.deleteCharAt(lastComma);
		content.deleteCharAt(lastComma);
		
		content.append("}");
		
		return content.toString();
	}
	
	/**
	 * �듅�젙 �궎瑜� 釉붾옓由ъ뒪�듃濡� 吏��젙�븳�떎.
	 * 釉붾옓由ъ뒪�듃濡� 吏��젙�맂 �궎�뒗 toJson�쓣 �븷 �븣 �젣�쇅�맂�떎.
	 * 
	 * @param blackKey
	 * @return
	 */
	public boolean setJsonBlackListKey(String blackKey) {
		if (blackKey == null || blackKey.trim().length() == 0) {
			return false;
			
		} else {
			blackKey = blackKey.trim();
		}
		
		if (BlackListKey == null) {
			BlackListKey = new StringList();
		}
		
		BlackListKey.add(blackKey);
		return true;
	}
	
	/**
	 * 紐⑤뱺 key瑜� �돹�몴瑜� delimeter濡� �븯�뿬 �뀓�뒪�듃濡� 留뚮뱺�떎.
	 * @return
	 */
	public String getKeyListText() {
		
		StringBuffer keyListText = new StringBuffer();
		
		String oneKey = "";
		
		Iterator<String> iter = keySet().iterator();
		while (iter.hasNext()) {
			oneKey = iter.next();
			if (oneKey == null || oneKey.length() == 0) {
				continue;
			}
			
			keyListText.append(oneKey);
			keyListText.append(", ");
		}
		
		int lastComma = keyListText.lastIndexOf(", ");
		keyListText.deleteCharAt(lastComma);
		keyListText.deleteCharAt(lastComma);
		
		return keyListText.toString();
	}
}