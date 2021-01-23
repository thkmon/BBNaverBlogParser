package com.thkmon.blogparser.wordpress;

import java.sql.Connection;
import java.sql.SQLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.thkmon.blogparser.database.SimpleDBMapper;
import com.thkmon.blogparser.database.SimpleDBUtil;
import com.thkmon.blogparser.prototype.ObjList;
import com.thkmon.blogparser.prototype.StringMap;

public class WordpressMapper {
	
	
	/**
	 * ���̹� ��α� ����Ʈ�� �����ͼ� ���������� DB �� insert �Ѵ�.
	 * 
	 * @param wordpressUrl
	 * @param naverUserId
	 * @param targetMap
	 * @throws SQLException
	 * @throws Exception
	 */
	public void insertWordpressPostFromNaverBlogPost(String wordpressUrl, String naverUserId, StringMap targetMap) throws SQLException, Exception {
		if (wordpressUrl == null || wordpressUrl.length() == 0) {
			return;
		}
		
		if (naverUserId == null || naverUserId.length() == 0) {
			return;
		}
		
		if (targetMap == null) {
			return;
		}

		String postNo = targetMap.get("logNo");
		if (postNo == null || postNo.length() == 0) {
			return;
		}

		Connection conn = null;
		SimpleDBMapper mapper = new SimpleDBMapper();

		try {
			conn = SimpleDBUtil.getConnection();

			{
				String query = " SELECT COUNT(*) FROM wp_posts WHERE post_name = ? ";

				ObjList objList = new ObjList();
				objList.add(String.valueOf(postNo));

				int rowCount = mapper.selectFirstInt(conn, query, objList, 0);
				if (rowCount > 0) {
					return;
				}
			}
			
			
			// https://blog.naver.com/naverId/" + postNo
			String urlString = "https://blog.naver.com/PostView.nhn?blogId=" + naverUserId + "&logNo=" + postNo + "&redirect=Dlog&widgetTypeCall=true&directAccess=false";

			Document doc = Jsoup.connect(urlString).get();
			Elements postViewAreas = doc.select("#postViewArea");
			
			
			// ����Ʈ������ 3.0 ����
			boolean isSmartEditor3 = false;
			if (postViewAreas == null || postViewAreas.size() == 0) {
				isSmartEditor3 = true;
			}
			
			
			// ����Ʈ ���� ��������
			String strHtml = "";
			if (!isSmartEditor3) {
				Element postViewArea = postViewAreas.get(0);
				strHtml = postViewArea.html();
			} else {
				postViewAreas = doc.select(".se_component_wrap");
				Element postViewArea = postViewAreas.get(1);
				strHtml = postViewArea.html();
			}
			
			if (strHtml == null || strHtml.length() == 0) {
				System.out.println("����Ʈ ������ ������ �� �����ϴ�. (postNo : " + postNo + ")");
				return;
			}
			
			
			int nextID = 0;
			{
				String query = " SELECT MAX(ID) + 1 FROM wp_posts ";
				nextID = mapper.selectFirstInt(conn, query, null, 0);
			}

			{
				StringBuffer buff = new StringBuffer();

				buff.append(" INSERT INTO ");
				buff.append(" wp_posts ( ");
				buff.append(" 	ID, ");
				buff.append(" 	post_author, ");
				buff.append(" 	post_date, ");
				buff.append(" 	post_date_gmt, ");
				buff.append(" 	post_content, ");
				buff.append(" 	post_title, ");
				buff.append(" 	post_excerpt, ");
				buff.append(" 	post_status, ");
				buff.append(" 	comment_status, ");
				buff.append(" 	ping_status, ");
				buff.append(" 	post_password, ");
				buff.append(" 	post_name, ");
				buff.append(" 	to_ping, ");
				buff.append(" 	pinged, ");
				buff.append(" 	post_modified, ");
				buff.append(" 	post_modified_gmt, ");
				buff.append(" 	post_content_filtered, ");
				buff.append(" 	post_parent, ");
				buff.append(" 	guid, ");
				buff.append(" 	menu_order, ");
				buff.append(" 	post_type, ");
				buff.append(" 	post_mime_type, ");
				buff.append(" 	comment_count ");
				buff.append(" ) values ( ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	? ");
				buff.append(" ) ");

				String query = buff.toString();

				
				String strTitle = targetMap.get("title");
				String strDate = targetMap.get("addDate");
				String[] strDateArr = strDate.split("\\.");
				
				try {
					Integer.parseInt(strDateArr[0].trim());
				} catch (NumberFormatException e) {
					System.err.println("�Ϸ簡 ������ ���� ����Ʈ�� ���ε��� �� �����ϴ�.");
					return;
				}
				
				String strYear = String.format("%04d", Integer.parseInt(strDateArr[0].trim()));
				String strMonth = String.format("%02d", Integer.parseInt(strDateArr[1].trim()));
				String strDay = String.format("%02d", Integer.parseInt(strDateArr[2].trim()));

				String strDate2 = strYear + "-" + strMonth + "-" + strDay + " " + "00:00:00";

				
				ObjList objList = new ObjList();
				objList.add(String.valueOf(nextID));
				objList.add(String.valueOf("1"));
				objList.add(String.valueOf(strDate2));
				objList.add(String.valueOf(strDate2));
				objList.add(String.valueOf(strHtml));
				objList.add(String.valueOf(strTitle));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf("publish"));
				objList.add(String.valueOf("open"));
				objList.add(String.valueOf("open"));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf(postNo));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf(strDate2));
				objList.add(String.valueOf(strDate2));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf("0"));
				objList.add(String.valueOf(wordpressUrl + "/?p=" + postNo));
				objList.add(String.valueOf("0"));
				objList.add(String.valueOf("post"));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf("0"));

				boolean isInserted = mapper.insert(conn, query, objList);
				System.out.println("isInserted ; " + isInserted);
			}

			SimpleDBUtil.commitAndClose(conn);

		} catch (SQLException e) {
			throw e;
			
		} catch (Exception e) {
			throw e;

		} finally {
			SimpleDBUtil.rollbackAndClose(conn);
		}
	}
	
	
	/**
	 * ���̹� ��α� ����Ʈ�� �����ͼ� ���������� DB �� insert �Ѵ�.
	 * 
	 * @param wordpressUrl
	 * @param naverUserId
	 * @param postNo
	 * @throws SQLException
	 * @throws Exception
	 */
	public void updateWordpressPostFromNaverBlogPost(String wordpressUrl, String naverUserId, String postNo) throws SQLException, Exception {
		if (wordpressUrl == null || wordpressUrl.length() == 0) {
			return;
		}
		
		if (naverUserId == null || naverUserId.length() == 0) {
			return;
		}
		
		if (postNo == null || postNo.length() == 0) {
			return;
		}
		
		/*
		if (targetMap == null) {
			return;
		}

		String postNo = targetMap.get("logNo");
		if (postNo == null || postNo.length() == 0) {
			return;
		}
		*/
		
		Connection conn = null;
		SimpleDBMapper mapper = new SimpleDBMapper();
		
		try {
			conn = SimpleDBUtil.getConnection();
			
			String postID  = "";
			{
				String query = " SELECT ID FROM wp_posts WHERE post_name = ? ";

				ObjList objList = new ObjList();
				objList.add(String.valueOf(postNo));

				postID = mapper.selectFirstString(conn, query, objList, "");
				if (postID == null || postID.length() == 0) {
					System.out.println("������Ʈ�� �ο츦 ã�� �� �����ϴ�. (postNo : " + postNo + ")");
					return;
				}
			}

			// https://blog.naver.com/naverId/" + postNo
			String urlString = "https://blog.naver.com/PostView.nhn?blogId=" + naverUserId + "&logNo=" + postNo + "&redirect=Dlog&widgetTypeCall=true&directAccess=false";

			Document doc = Jsoup.connect(urlString).get();
			Elements postViewAreas = doc.select("#postViewArea");
			
			
			// ����Ʈ������ 3.0 ����
			boolean isSmartEditor3 = false;
			if (postViewAreas == null || postViewAreas.size() == 0) {
				isSmartEditor3 = true;
			}
			
			
			// ����Ʈ ���� ��������
			String strHtml = "";
			if (!isSmartEditor3) {
				Element postViewArea = postViewAreas.get(0);
				strHtml = postViewArea.html();
			} else {
				postViewAreas = doc.select(".se_component_wrap");
				Element postViewArea = postViewAreas.get(1);
				strHtml = postViewArea.html();
			}
			
			if (strHtml == null || strHtml.length() == 0) {
				System.out.println("����Ʈ ������ ������ �� �����ϴ�. (postNo : " + postNo + ")");
				return;
			}
			
			
			// ����Ʈ ���� ��������
			String strTitle = "";
			try {
				if (!isSmartEditor3) {
					strTitle = doc.select(".itemSubjectBoldfont").get(0).html().trim();
				}
			} catch (NullPointerException e) {
				// ������ ���������� ��� ���� �����ϰ� ������ ������Ʈ ����
				strTitle = "";
			} catch (Exception e) {
				// ������ ���������� ��� ���� �����ϰ� ������ ������Ʈ ����
				strTitle = "";
			}
			
			
			{
				StringBuffer buff = new StringBuffer();

				buff.append(" UPDATE wp_posts ");
				buff.append(" SET ");
				buff.append(" 	post_author = ?, ");
//				buff.append(" 	post_date = ?, ");
//				buff.append(" 	post_date_gmt = ?, ");
				buff.append(" 	post_content = ?, ");
				if (strTitle != null && strTitle.length() > 0) {
					buff.append(" 	post_title = ?, ");
				}
				buff.append(" 	post_excerpt = ?, ");
				buff.append(" 	post_status = ?, ");
				buff.append(" 	comment_status = ?, ");
				buff.append(" 	ping_status = ?, ");
				buff.append(" 	post_password = ?, ");
//				buff.append(" 	post_name = ?, ");
				buff.append(" 	to_ping = ?, ");
				buff.append(" 	pinged = ?, ");
//				buff.append(" 	post_modified = ?, ");
//				buff.append(" 	post_modified_gmt = ?, ");
				buff.append(" 	post_content_filtered = ?, ");
				buff.append(" 	post_parent = ?, ");
				buff.append(" 	guid = ?, ");
				buff.append(" 	menu_order = ?, ");
				buff.append(" 	post_type = ?, ");
				buff.append(" 	post_mime_type = ?, ");
				buff.append(" 	comment_count = ? ");
				buff.append(" WHERE ID = ? AND post_name = ? ");

				String query = buff.toString();
				
				
				ObjList objList = new ObjList();
				objList.add(String.valueOf("1"));
//				objList.add(String.valueOf(strDate2));
//				objList.add(String.valueOf(strDate2));
				objList.add(String.valueOf(strHtml));
				if (strTitle != null && strTitle.length() > 0) {
					objList.add(String.valueOf(strTitle));
				}
				objList.add(String.valueOf(""));
				objList.add(String.valueOf("publish"));
				objList.add(String.valueOf("open"));
				objList.add(String.valueOf("open"));
				objList.add(String.valueOf(""));
//				objList.add(String.valueOf(postNo));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf(""));
//				objList.add(String.valueOf(strDate2));
//				objList.add(String.valueOf(strDate2));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf("0"));
				objList.add(String.valueOf(wordpressUrl + "/?p=" + postNo));
				objList.add(String.valueOf("0"));
				objList.add(String.valueOf("post"));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf("0"));
				
				objList.add(String.valueOf(postID));
				objList.add(String.valueOf(postNo));

				boolean isUpdated = mapper.insert(conn, query, objList);
				System.out.println("isUpdated ; " + isUpdated);
			}

			SimpleDBUtil.commitAndClose(conn);

		} catch (SQLException e) {
			throw e;
			
		} catch (Exception e) {
			throw e;

		} finally {
			SimpleDBUtil.rollbackAndClose(conn);
		}
	}
}