package com.thkmon.blogparser.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import com.thkmon.blogparser.prototype.BasicMap;
import com.thkmon.blogparser.prototype.BasicMapList;
import com.thkmon.blogparser.prototype.ObjList;

public class SimpleDBMapper {
	
	public int selectFirstInt(Connection conn, String query, ObjList objList, int defaultInt) throws Exception {
		BasicMapList resultList = select(conn, query, objList);
		if (resultList != null && resultList.size() > 0) {
			return resultList.get(0).getInt(0, defaultInt);
		}
		
		return defaultInt;
	}
	
	
	public String selectFirstString(Connection conn, String query, ObjList objList, String defaultString) throws Exception {
		BasicMapList resultList = select(conn, query, objList);
		if (resultList != null && resultList.size() > 0) {
			return resultList.get(0).getString(0, defaultString);
		}
		
		return defaultString;
	}
	
	
	public BasicMap selectFirstRow(Connection conn, String query, ObjList objList) throws Exception {
		BasicMapList resultList = select(conn, query, objList);
		if (resultList != null && resultList.size() > 0) {
			return resultList.get(0);
		}
		
		return null;
	}
	
	
	public BasicMapList select(Connection conn, String query, ObjList objList) throws Exception {
		if (conn == null) {
			throw new Exception("SimpleDBMapper select : conn is null.");
		}
		
		BasicMapList basicMapList = null;
		
		try {
			if (query == null || query.trim().length() == 0) {
				return null;
			}
			
			if (query.indexOf("@rownum") > -1) {
				this.update(conn, " set @rownum:=0 ", null);
			}
			
			System.out.println(getQueryString(query, objList));
			
			PreparedStatement pst = conn.prepareStatement(query);
			
			Object oneObj = null;
			if (objList != null && objList.size() > 0) {
				int size = objList.size();
				for (int i=0; i<size; i++) {
					oneObj = objList.get(i);
					if (oneObj == null) {
						pst.setNull(i+1, 0);
					} else if (oneObj instanceof String) {
						pst.setString(i+1, String.valueOf(oneObj));
					} else {
						pst.setInt(i+1, Integer.parseInt(String.valueOf(oneObj)));
					}
					
				}
			}
			
			ResultSet rs = pst.executeQuery();
			
			ResultSetMetaData metaData = rs.getMetaData();
			
			int colCount = metaData.getColumnCount();
			
			if (colCount < 0) {
				return null;
			}

			basicMapList = new BasicMapList();
			
			while (rs.next()) {
				BasicMap basicMap = new BasicMap();
				for (int i=0; i<colCount; i++) {
					basicMap.setString(metaData.getColumnName(i+1).toLowerCase(), rs.getString(i+1));
				}
				basicMapList.add(basicMap);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
			
		} finally {
		}
		
		return basicMapList;
	}
	
	
	public InputStream selectBlob(Connection conn, String query, ObjList objList, String blobColumnName) throws Exception {

		InputStream inputStream = null;
		
		try {
			if (query == null || query.trim().length() == 0) {
				return null;
			}
			
			System.out.println(getQueryString(query, objList));
			
			PreparedStatement pst = conn.prepareStatement(query);
			
			Object oneObj = null;
			if (objList != null && objList.size() > 0) {
				int size = objList.size();
				for (int i=0; i<size; i++) {
					oneObj = objList.get(i);
					if (oneObj == null) {
						pst.setNull(i+1, 0);
					} else if (oneObj instanceof String) {
						pst.setString(i+1, String.valueOf(oneObj));
					} else {
						pst.setInt(i+1, Integer.parseInt(String.valueOf(oneObj)));
					}
					
				}
			}
			
			ResultSet rs = pst.executeQuery();
			
//				ResultSetMetaData metaData = rs.getMetaData();
			
//				String fistColumnName = "";
			while (rs.next()) {
//					fistColumnName = metaData.getColumnName(1);
//					inputStream = rs.getBinaryStream(fistColumnName);
				
				inputStream = rs.getBinaryStream(blobColumnName);
				break;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
			
		} finally {
		}
		
		return inputStream;
	}
	
	
	private String getQueryString(String query, ObjList objList) {
		
		if (query == null || query.length() == 0) {
			return "";
		}
		
		query = query.replace("\t", " ").replace("\r", "").replace("\n", "");
		while (query.indexOf("  ") > -1) {
			query = query.replace("  ", " ");
		}
		
//		int mapIdx = 0;
//		
//		int quesIdx = query.indexOf("?");
//		while (quesIdx > -1) {
//			if (objList.size() <= mapIdx) {
//				break;
//			}
//			query = query.substring(0, quesIdx) + "'" + objList.get(mapIdx) + "'" + query.substring(quesIdx + 1);
//			mapIdx++;
//			quesIdx = query.indexOf("?");
//		}
		
		// 로그에 쿼리 쓸 때 물음표 뒤에서부터 매핑하도록 수정
		if (objList != null && objList.size() > 0) {
			int mapIdx = objList.size() - 1;
			
			int lastIndex = query.length() - 1;
			for (int i=lastIndex; i>=0; i--) {
				if (query.substring(i, i+1).equals("?")) {
					query = query.substring(0, i) + "'" + objList.get(mapIdx) + "'" + query.substring(i + 1);
					mapIdx--;
					if (mapIdx < 0) {
						break;
					}
				}
			}
		}
		
		query = query.trim();
		
		// 쿼리 출력 시 500자까지만 출력
		if (query.length() > 500) {
			query = query.substring(0, 500) + "...";
		}
		
		return "getQueryString : " + query + ";";
	}
	
	
	public boolean insert(Connection conn, String query, ObjList objList) throws Exception {
		if (conn == null) {
			throw new Exception("SimpleDBMapper insert : conn is null.");
		}
		
		PreparedStatement pst = null;
		
		try {
			if (query == null || query.trim().length() == 0) {
				return false;
			}
			
			System.out.println(getQueryString(query, objList));
			
			pst = conn.prepareStatement(query);
			
			if (objList != null && objList.size() > 0) {
				int size = objList.size();
				
				Object oneObj = null;
				for (int i=0; i<size; i++) {
					oneObj = objList.get(i);
					if (oneObj == null) {
						pst.setNull(i+1, 0);
					} else if (oneObj instanceof String) {
						pst.setString(i+1, String.valueOf(oneObj));
					} else if (oneObj instanceof File) {
						FileInputStream input = new FileInputStream((File)oneObj);
						pst.setBinaryStream(i+1, input);
					} else {
						pst.setInt(i+1, Integer.parseInt(String.valueOf(oneObj)));
					}
				}
			}
			
			int result = pst.executeUpdate();
			
			if (result < 1) {
				return false;
			}

			SimpleDBUtil.close(pst);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
			
		} finally {
			SimpleDBUtil.close(pst);
		}
		
		return true;
	}
	
	
	public int update(Connection conn, String query, ObjList objList) throws Exception {
		if (conn == null) {
			throw new Exception("SimpleDBMapper update : conn is null.");
		}
		
		PreparedStatement pst = null;
		
		int updateCount = 0;
		
		try {
			if (query == null || query.trim().length() == 0) {
				return 0;
			}
			
			System.out.println(getQueryString(query, objList));
			
			pst = conn.prepareStatement(query);
			
			if (objList != null && objList.size() > 0) {
				int size = objList.size();
				
				Object oneObj = null;
				for (int i=0; i<size; i++) {
					oneObj = objList.get(i);
					if (oneObj == null) {
						pst.setNull(i+1, 0);
					} else if (oneObj instanceof String) {
						pst.setString(i+1, String.valueOf(oneObj));
					} else {
						pst.setInt(i+1, Integer.parseInt(String.valueOf(oneObj)));
					}
				}
			}
			
			updateCount = pst.executeUpdate();
			
			if (updateCount < 1) {
				return 0;
			}

			SimpleDBUtil.close(pst);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
			
		} finally {
			SimpleDBUtil.close(pst);
		}
		
		return updateCount;
	}
}