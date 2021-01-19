package com.thkmon.blogparser.prototype;

import java.util.ArrayList;

public class StringMapList extends ArrayList<StringMap> {
	
	
	/**
	 * 중복 없이 추가
	 * 
	 * @param map
	 * @param uniqueKey
	 * @return
	 */
	public boolean addNotDupl(StringMap map, String uniqueKey) {
		if (map == null) {
			return false;
		}
		
		if (uniqueKey == null || uniqueKey.length() == 0) {
			return false;
		}
		
		String newValue = map.get(uniqueKey);
		if (newValue == null || newValue.length() == 0) {
			return false;
		}
		
		boolean bResult = false;
		
		int count = this.size();
		if (count > 0) {
			boolean bFound = false;
			
			StringMap oneMap = null;
			String oldValue = "";
			for (int i=0; i<count; i++) {
				oneMap = this.get(i);
				if (oneMap == null) {
					continue;
				}
				
				oldValue = oneMap.get(uniqueKey);
				if (newValue.equals(oldValue)) {
					bFound = true;
					break;
				}
			}
			
			if (!bFound) {
				bResult = this.add(map);
			}
		} else {
			bResult = this.add(map);
		}
		
		return bResult;
	}
	
	
	/**
	 * 리스트 순서 거꾸로 만들어서 리턴
	 * 
	 * @return
	 */
	public StringMapList reverse() {
		StringMapList resultList = new StringMapList();
		
		int count = this.size();
		if (count > 0) {
			int lastIndex = count - 1;
			for (int i=lastIndex; i>=0; i--) {
				resultList.add(this.get(i));
			}
		}
		
		return resultList;
	}
	
	
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		
		int listCount = this.size();
		if (listCount > 0) {
			for (int i=0; i<listCount; i++) {
				if (i > 0) {
					buff.append("\n");
				}
				buff.append(i + " : " + this.get(i).toString());
			}
		}
		
		return buff.toString();
	}
	
	
	public StringMap find(String key, String value) {
		StringMap oneMap = null;
		String oneValue = null;
		
		int count = this.size();
		for (int i=0; i<count; i++) {
			oneMap = this.get(i);
			if (oneMap == null) {
				continue;
			}
			
			oneValue = oneMap.get(key);
			if (oneValue == null || oneValue.length() == 0) {
				continue;
			}
			
			if (oneValue.equals(value)) {
				return oneMap;
			}
		}
		
		return null;
	}
}